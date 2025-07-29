package no.nav.hm.grunndata.db.index.product

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.data.model.Sort.Order
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.indexSettingsMap
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
    private val indexer: OpensearchIndexer) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexer::class.java)
        val settings = ProductIndexer::class.java
            .getResource("/opensearch/products_settings.json")!!.readText()
        val mapping = ProductIndexer::class.java
            .getResource("/opensearch/products_mapping.json")!!.readText()
    }

    val aliasIndexName = indexSettingsMap[IndexType.PRODUCT]!!.aliasIndexName

    val size: Int = 5000

    fun count() = indexer.docCount(aliasIndexName)

    suspend fun reIndex(alias: Boolean, from : LocalDateTime?=null) {
        val indexName = createIndexName(aliasIndexName)
        if (!indexer.indexExists(indexName)) {
            indexer.createIndex(indexName, settings, mapping)
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
                    indexType = IndexType.PRODUCT,
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
            indexer.updateAlias(aliasName = aliasIndexName, indexName = indexName)
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
                indexType = IndexType.PRODUCT,
                indexName = aliasIndexName,
            )}
            if (products.isNotEmpty()) {
                LOG.info("indexing ${products.size} products to ${aliasIndexName}")
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
                indexType = IndexType.PRODUCT,
                indexName = aliasIndexName
            )}
            LOG.info("indexing ${products.size} products to ${aliasIndexName}")
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
                indexType = IndexType.PRODUCT,
                indexName = aliasIndexName
            )}
            if (products.isNotEmpty()) {
                LOG.info("indexing ${products.size} products to ${aliasIndexName}")
                indexer.indexDoc(products)
            }
            page = productService.findProducts(criteria = ProductCriteria(isoCategory = iso), Pageable.from(++pageNumber, size, Sort.of(Order.asc("updated"))))
        }
    }

    fun updateAlias(indexName: String) = indexer.updateAlias(aliasName = aliasIndexName, indexName = indexName)
    fun getAlias() = indexer.getAlias(aliasIndexName)
    fun delete(id: UUID) = indexer.delete(id.toString(), aliasIndexName)
}
