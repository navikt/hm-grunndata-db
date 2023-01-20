package no.nav.hm.grunndata.db.indexer

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import org.opensearch.action.bulk.BulkResponse
import org.slf4j.LoggerFactory

@Singleton
class ProductIndexer(private val indexer: Indexer,
                     @Value("\${ALIASNAME:product}") private val aliasName: String,
                     @Value("\${INDEXNAME:product_2022}") private val indexName: String ) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexer::class.java)
        private val settings = ProductIndexer::class.java
            .getResource("/opensearch/products_settings.json")?.readText()
        private val mapping = ProductIndexer::class.java
            .getResource("/opensearch/products_mapping.json")?.readText()
    }

    init {

        try {
            indexer.initIndex(indexName, settings, mapping)
            indexer.initAlias(aliasName,indexName)
        } catch (e: Exception) {
            LOG.error("OpenSearch might not be ready ${e.message}, will wait 10s and retry")
            Thread.sleep(10000)
            indexer.initIndex(indexName, settings, mapping)
            indexer.initAlias(aliasName,indexName)
        }
    }

    fun index(docs: List<ProductDoc>): BulkResponse = indexer.index(docs, indexName)


    fun index(doc: ProductDoc): BulkResponse = indexer.index(listOf(doc), indexName)


    fun index(doc: ProductDoc, indexName: String): BulkResponse =
        indexer.index(listOf(doc), indexName)


    fun index(docs: List<ProductDoc>, indexName: String): BulkResponse =
        indexer.index(docs,indexName)


    fun createIndex(indexName: String): Boolean = indexer.createIndex(indexName, settings, mapping)

    fun updateAlias(indexName: String): Boolean = indexer.updateAlias(indexName,aliasName)

    fun indexExists(indexName: String): Boolean = indexer.indexExists(indexName)



}
