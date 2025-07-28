package no.nav.hm.grunndata.db.index.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.data.model.Sort.Order
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.IndexItemSupport
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.db.product.ProductCriteria
import no.nav.hm.grunndata.db.product.ProductService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

@Singleton
class ProductIndexer(
    private val isoCategoryService: IsoCategoryService,
    private val productService: ProductService,
    private val indexer: OpensearchIndexer): IndexItemSupport {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexer::class.java)
        private val settings = ProductIndexer::class.java
            .getResource("/opensearch/products_settings.json")!!.readText()
        private val mapping = ProductIndexer::class.java
            .getResource("/opensearch/products_mapping.json")!!.readText()

    }

    val size: Int = 5000

    fun count() = indexer.docCount(getAliasIndexName())

    suspend fun reIndex(alias: Boolean, from : LocalDateTime?=null) {
        val indexName = createIndexName(getAliasIndexName())
        if (!indexer.indexExists(indexName)) {
            indexer.createIndex(indexName, getSettings(), getMappings())
        }
        var updated = from ?: LocalDateTime.now().minusYears(1000)
        var page = productService.findProducts(criteria = ProductCriteria(updated = updated), pageable = Pageable.from(
            0, size, Sort.of(Order.asc( "updated"))))
        var lastId: UUID? = null
        while (page.numberOfElements > 0) {
            val products = page.content
                .map { IndexDoc(
                    id = it.id.toString(),
                    doc = it.toDoc(isoCategoryService),
                    indexType = getIndexType(),
                    indexName = indexName
                )}
            LOG.info("indexing ${products.size} products to $indexName")
            if (products.isNotEmpty()) indexer.indexDoc(products)
            val last = page.last()
            if (updated.equals(last.updated) && last.id == lastId) {
                LOG.info("Last updated ${last.updated} ${last.id} is the same, increasing last updated")
                updated = updated.plusNanos(1000000)
            } else {
                lastId = last.id
                updated = last.updated
            }
            LOG.info("updated is now: $updated")
            page = productService.findProducts(criteria = ProductCriteria(updated = updated), pageable = Pageable.from(
                0, size, Sort.of(Order.asc( "updated"))))
        }
        if (alias) {
            indexer.updateAlias(aliasName = getAliasIndexName(), indexName = indexName)
        }
    }

    suspend fun reIndexBySupplierId(supplierId: UUID) {
        var pageNumber = 0
        var page = productService.findProducts(criteria = ProductCriteria(supplierId= supplierId), Pageable.from(
           pageNumber, size, Sort.of(Order.asc("updated"))))
        while (page.numberOfElements > 0) {
            val products = page.content.map { IndexDoc(
                id = it.id.toString(),
                doc = it.toDoc(isoCategoryService),
                indexType = getIndexType(),
                indexName = getAliasIndexName(),
            )}
            if (products.isNotEmpty()) {
                LOG.info("indexing ${products.size} products to ${getAliasIndexName()}")
                indexer.indexDoc(products)
            }
            page = productService.findProducts(criteria = ProductCriteria(supplierId= supplierId), Pageable.from(
                ++pageNumber, size, Sort.of(Order.asc("updated"))))
        }
        LOG.info("finished indexing products for supplier $supplierId")
    }

    suspend fun reIndexBySeriesId(seriesId: UUID) {
        val page = productService.findProducts(criteria = ProductCriteria(seriesUUID = seriesId),
            pageable = Pageable.from(0, size, Sort.of(Order.asc("updated")))
        )
        if (page.numberOfElements > 0) {
            val products = page.content.map { IndexDoc(
                id = it.id.toString(),
                doc = it.toDoc(isoCategoryService),
                indexType = getIndexType(),
                indexName = getAliasIndexName()
            )}
            LOG.info("indexing ${products.size} products to ${getAliasIndexName()}")
            indexer.indexDoc(products)
        }
    }

    suspend fun reIndexByIsoCategory(iso: String) {
        var pageNumber = 0
        var page = productService.findProducts(criteria = ProductCriteria(isoCategory = iso), Pageable.from(pageNumber, size, Sort.of(Order.asc("updated"))))
        while (page.numberOfElements > 0) {
            val products = page.content.map { IndexDoc(
                id = it.id.toString(),
                doc = it.toDoc(isoCategoryService),
                indexType = getIndexType(),
                indexName = getAliasIndexName()
            )}
            if (products.isNotEmpty()) {
                LOG.info("indexing ${products.size} products to ${getAliasIndexName()}")
                indexer.indexDoc(products)
            }
            page = productService.findProducts(criteria = ProductCriteria(isoCategory = iso), Pageable.from(++pageNumber, size, Sort.of(Order.asc("updated"))))
        }
    }

    override fun getAliasIndexName(): String = "products"

    override fun getMappings(): String = mapping

    override fun getSettings(): String = settings

    override fun getIndexType(): IndexType = IndexType.PRODUCT

    override fun getSearchDocClassType(): Class<out SearchDoc> = ProductDoc::class.java

    fun updateAlias(indexName: String) = indexer.updateAlias(aliasName = getAliasIndexName(), indexName = indexName)
    fun getAlias() = indexer.getAlias(getAliasIndexName())
    fun delete(id: UUID) = indexer.delete(id.toString(), getAliasIndexName())
}
