package no.nav.hm.grunndata.db.indexer

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import org.opensearch.action.admin.indices.alias.IndicesAliasesRequest
import org.opensearch.action.admin.indices.alias.get.GetAliasesRequest
import org.opensearch.action.bulk.BulkRequest
import org.opensearch.action.bulk.BulkResponse
import org.opensearch.action.index.IndexRequest
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestHighLevelClient
import org.opensearch.client.indices.CreateIndexRequest
import org.opensearch.client.indices.GetIndexRequest
import org.opensearch.common.xcontent.XContentType
import org.opensearch.rest.RestStatus
import org.slf4j.LoggerFactory

@Singleton
class ProductIndexer(private val indexer: Indexer,
                     @Value("\${ALIASNAME:product}") private val aliasName: String,
                     @Value("\${INDEXNAME:product_2022}") private val indexName: String ) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexer::class.java)
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

    fun index(docs: List<ProductDoc>): BulkResponse = indexer.index(docs, indexName)


    fun index(doc: ProductDoc): BulkResponse = indexer.index(listOf(doc), indexName)


    fun index(doc: ProductDoc, indexName: String): BulkResponse =
        indexer.index(listOf(doc), indexName)


    fun index(docs: List<ProductDoc>, indexName: String): BulkResponse =
        indexer.index(docs,indexName)


    fun createIndex(indexName: String): Boolean = indexer.createIndex(indexName)

    fun updateAlias(indexName: String): Boolean = indexer.updateAlias(indexName,aliasName)



}
