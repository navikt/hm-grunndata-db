package no.nav.hm.grunndata.db.index.product

import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.data.model.Sort.Order
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.Indexer
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.db.product.ProductCriteria
import no.nav.hm.grunndata.db.product.ProductService
import no.nav.hm.grunndata.rapid.dto.ProductStatus

import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.UUID

@Singleton
class ProductIndexer(
    @Value("\${products.aliasName}") private val aliasName: String,
    private val isoCategoryService: IsoCategoryService,
    private val productService: ProductService,
    private val indexableItem: ProductIndexItem,
    private val indexer: Indexer) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexer::class.java)
    }

    val size: Int = 5000

    fun count() = indexer.docCount()

    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(indexableItem.getAliasIndexName())
        if (!indexer.indexExists(indexName)) {
            indexer.createIndex(indexName, indexableItem.getSettings(), indexableItem.getMappings())
        }
        var updated = LocalDateTime.now().minusYears(1000)
        var page = productService.findProducts(criteria = ProductCriteria(updated = updated), pageable = Pageable.from(
            0, size, Sort.of(Sort.Order.asc( "updated"))))
        var lastId: UUID? = null
        while (page.numberOfElements > 0) {
            val products = page.content
                .map { it.toDoc(isoCategoryService) }.filter {
                    it.status != ProductStatus.DELETED
                }
            LOG.info("indexing ${products.size} products to $indexName")
            if (products.isNotEmpty()) indexer.index(products, indexName)
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
                0, size, Sort.of(Sort.Order.asc( "updated"))))
        }
        if (alias) {
            indexer.updateAlias(indexName = indexName)
        }
    }

    suspend fun reIndexBySupplierId(supplierId: UUID) {
        var pageNumber = 0
        var page = productService.findProducts(criteria = ProductCriteria(supplierId= supplierId), Pageable.from(
           pageNumber, size, Sort.of(Order.asc("updated"))))
        while (page.numberOfElements > 0) {
            val products = page.content.map { it.toDoc(isoCategoryService) }.filter {
                it.status != ProductStatus.DELETED
            }
            if (products.isNotEmpty()) {
                LOG.info("indexing ${products.size} products to $aliasName")
                indexer.index(products, aliasName)
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
            val products = page.content.map { it.toDoc(isoCategoryService) }.filter {
                it.status != ProductStatus.DELETED
            }
            LOG.info("indexing ${products.size} products to $aliasName")
            indexer.index(products, aliasName)
        }
    }

    suspend fun reIndexByIsoCategory(iso: String) {
        var pageNumber = 0
        var page = productService.findProducts(criteria = ProductCriteria(isoCategory = iso), Pageable.from(pageNumber, size, Sort.of(Order.asc("updated"))))
        while (page.numberOfElements > 0) {
            val products = page.content.map { it.toDoc(isoCategoryService) }.filter {
                it.status != ProductStatus.DELETED
            }
            if (products.isNotEmpty()) {
                LOG.info("indexing ${products.size} products to $aliasName")
                indexer.index(products, aliasName)
            }
            page = productService.findProducts(criteria = ProductCriteria(isoCategory = iso), Pageable.from(++pageNumber, size, Sort.of(Order.asc("updated"))))
        }
    }

}
