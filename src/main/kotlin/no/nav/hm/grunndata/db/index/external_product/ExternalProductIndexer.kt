package no.nav.hm.grunndata.db.index.external_product

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
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
class ExternalProductIndexer(
    private val isoCategoryService: IsoCategoryService,
    private val productService: ProductService,
    private val indexer: OpensearchIndexer
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ExternalProductIndexer::class.java)
        val settings = ExternalProductIndexer::class.java
            .getResource("/opensearch/external_products_settings.json")!!.readText()
        val mapping = ExternalProductIndexer::class.java
            .getResource("/opensearch/external_products_mapping.json")!!.readText()
    }

    val aliasIndexName = indexSettingsMap[IndexType.EXTERNAL_PRODUCT]!!.aliasIndexName

    suspend fun reIndex(alias: Boolean, from: LocalDateTime? = null, size: Int = 3000) {
        val indexName = createIndexName(aliasIndexName)
        if (!indexer.indexExists(indexName)) {
            LOG.info("creating index $indexName")
            indexer.createIndex(indexName, settings, mapping)
        }
        var updated = from ?: LocalDateTime.now().minusYears(30)
        LOG.info("reindexing $indexName from $updated")
        var page = productService.findProducts(criteria = ProductCriteria(updated = updated), pageable = Pageable.from(
            0, size, Sort.of(Sort.Order.asc( "updated"))))
        var lastId: UUID? = null
        while(page.numberOfElements>0) {
            val products = page.content
                .map { IndexDoc(id = it.id.toString(), indexType = IndexType.EXTERNAL_PRODUCT, doc = it.toExternalDoc(isoCategoryService), indexName = indexName)}

            LOG.info("indexing ${products.size} products to $indexName")
            if (products.isNotEmpty()) indexer.indexDoc(products)
            val last = page.last()
            if (updated.equals(last.updated) && last.id == lastId) {
                LOG.info("Last updated ${last.updated} ${last.id} is the same, increasing last updated")
                updated = updated.plusNanos(1000000)
            }
            else {
                lastId = last.id
                updated = last.updated
            }
            LOG.info("updated is now: $updated")
            page = productService.findProducts(criteria = ProductCriteria(updated = updated), pageable = Pageable.from(
                0, size, Sort.of(Sort.Order.asc( "updated"))))
        }
        if (alias) {
            indexer.updateAlias(aliasIndexName, indexName)
        }
    }

    fun updateAlias(indexName: String) {
        indexer.updateAlias(aliasIndexName,indexName)
    }

    fun getAlias() = indexer.getAlias(aliasIndexName)

}
