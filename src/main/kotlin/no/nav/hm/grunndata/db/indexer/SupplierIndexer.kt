package no.nav.hm.grunndata.db.indexer

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import org.opensearch.action.bulk.BulkResponse
import org.slf4j.LoggerFactory

@Singleton
class SupplierIndexer(private val indexer: Indexer,
                      @Value("\${SUPPLIER_ALIASNAME:supplier}") private val aliasName: String,
                      @Value("\${SUPPLIER_INDEXNAME:supplier_2022}") private val indexName: String ) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexer::class.java)
//        private val SETTINGS = ProductIndexer::class.java
//            .getResource("/opensearch/product-settings.json").readText()
//        private val MAPPING = ProductIndexer::class.java
//            .getResource("/opensearch/product-mapping.json").readText()
    }

    init {
        try {
            indexer.initIndex(indexName)
            indexer.initAlias(aliasName,indexName)
        } catch (e: Exception) {
            LOG.error("OpenSearch might not be ready ${e.message}, will wait 10s and retry")
            Thread.sleep(10000)
            indexer.initIndex(indexName)
            indexer.initAlias(aliasName,indexName)
        }
    }

    fun index(docs: List<SupplierDoc>): BulkResponse {
        return indexer.index(docs, indexName)
    }

    fun index(doc: SupplierDoc): BulkResponse {
        return indexer.index(listOf(doc), indexName)
    }

    fun index(doc: SupplierDoc, indexName: String): BulkResponse {
        return indexer.index(listOf(doc), indexName)
    }

    fun index(docs: List<SupplierDoc>, indexName: String): BulkResponse {
        return indexer.index(docs,indexName)
    }

    fun createIndex(indexName: String): Boolean = indexer.createIndex(indexName)

    fun updateAlias(indexName: String): Boolean = indexer.updateAlias(indexName,aliasName)
}
