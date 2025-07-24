package no.nav.hm.grunndata.db.index.external_product

import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.IndexName
import no.nav.hm.grunndata.db.index.Indexer
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.db.product.ProductCriteria
import no.nav.hm.grunndata.db.product.ProductService
import no.nav.hm.grunndata.rapid.dto.ProductStatus

import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*
import org.opensearch.client.opensearch.OpenSearchClient

@Singleton
class ExternalProductIndexer(
    @Value("\${external_products.aliasName}") private val aliasName: String,
    private val isoCategoryService: IsoCategoryService,
    private val productService: ProductService,
    private val client: OpenSearchClient
): Indexer(client, settings, mapping, aliasName) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ExternalProductIndexer::class.java)
        private val settings = ExternalProductIndexer::class.java
            .getResource("/opensearch/external_products_settings.json")!!.readText()
        private val mapping = ExternalProductIndexer::class.java
            .getResource("/opensearch/external_products_mapping.json")!!.readText()
    }

    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(IndexName.external_products)
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName, settings, mapping)
        }
        var updated =  LocalDateTime.now().minusYears(30)
        var page = productService.findProducts(criteria = ProductCriteria(updated = updated), pageable = Pageable.from(
            0, 3000, Sort.of(Sort.Order.asc( "updated"))))
        var lastId: UUID? = null
        while(page.numberOfElements>0) {
            val products = page.content
                .map { it.toExternalDoc(isoCategoryService) }.filter {
                    it.status != ProductStatus.DELETED
                }
            LOG.info("indexing ${products.size} products to $indexName")
            if (products.isNotEmpty()) index(products, indexName)
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
                0, 3000, Sort.of(Sort.Order.asc( "updated"))))
        }
        if (alias) {
            updateAlias(indexName)
        }
    }

}
