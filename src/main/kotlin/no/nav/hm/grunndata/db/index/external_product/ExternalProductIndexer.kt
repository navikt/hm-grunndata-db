package no.nav.hm.grunndata.db.index.external_product

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
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
import org.opensearch.client.opensearch.OpenSearchClient
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

@Singleton
class ExternalProductIndexer(
    private val isoCategoryService: IsoCategoryService,
    private val productService: ProductService,
    private val client: OpenSearchClient
): OpensearchIndexer(client), IndexItemSupport {
    companion object {
        private val LOG = LoggerFactory.getLogger(ExternalProductIndexer::class.java)
        private val settings = ExternalProductIndexer::class.java
            .getResource("/opensearch/external_products_settings.json")!!.readText()
        private val mapping = ExternalProductIndexer::class.java
            .getResource("/opensearch/external_products_mapping.json")!!.readText()

    }

    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(getAliasIndexName())
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName, getSettings(), getMappings())
        }
        var updated =  LocalDateTime.now().minusYears(30)
        var page = productService.findProducts(criteria = ProductCriteria(updated = updated), pageable = Pageable.from(
            0, 3000, Sort.of(Sort.Order.asc( "updated"))))
        var lastId: UUID? = null
        while(page.numberOfElements>0) {
            val products = page.content
                .map { IndexDoc(id = it.id, indexType = IndexType.EXTERNAL_PRODUCT, doc = it.toExternalDoc(isoCategoryService), indexName = indexName)}

            LOG.info("indexing ${products.size} products to $indexName")
            if (products.isNotEmpty()) indexDoc(products)
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
            updateAlias(getAliasIndexName(), indexName)
        }
    }

    fun updateAlias(indexName: String) {
        updateAlias(getAliasIndexName(),indexName)
    }

    fun getAlias() = getAlias(getAliasIndexName())

    override fun getAliasIndexName(): String = "external_products"

    override fun getMappings(): String = mapping

    override fun getSettings(): String = settings

    override fun getIndexType(): IndexType = IndexType.EXTERNAL_PRODUCT

    override fun getSearchDocClassType(): Class<out SearchDoc>  = ExternalProductDoc::class.java
}
