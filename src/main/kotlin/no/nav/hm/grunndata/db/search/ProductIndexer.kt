package no.nav.hm.grunndata.db.search

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
class ProductIndexer(private val client: RestHighLevelClient,
                     @Value("\${ALIASNAME:product}") private val aliasName: String,
                     @Value("\${INDEXNAME:product_2022}") private val indexName: String,
                     private val objectMapper: ObjectMapper) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexer::class.java)
//        private val SETTINGS = ProductIndexer::class.java
//            .getResource("/opensearch/product-settings.json").readText()
//        private val MAPPING = ProductIndexer::class.java
//            .getResource("/opensearch/product-mapping.json").readText()
    }

    init {
        try {
            initIndex(indexName)
        } catch (e: Exception) {
            LOG.error("OpenSearch might not be ready ${e.message}, will wait 10s and retry")
            Thread.sleep(10000)
            initIndex(indexName)
        }
    }

    private fun initIndex(indexName: String) {
        val indexRequest= GetIndexRequest(indexName)
        if (!client.indices().exists(indexRequest, RequestOptions.DEFAULT)) {
            if (createIndex(indexName))
                LOG.info("$indexName has been created")
            else
                LOG.error("Failed to create $indexName")
        }
        val aliasIndexRequest = GetAliasesRequest(aliasName)
        val response = client.indices().getAlias(aliasIndexRequest, RequestOptions.DEFAULT)
        if (response.status() == RestStatus.NOT_FOUND) {
            LOG.warn("Alias $aliasName is not pointing to any index, updating alias")
            updateAlias(indexName)
        }

    }

    fun updateAlias(indexName: String, removePreviousAliases: Boolean = false): Boolean {
        val remove = IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
            .index("$aliasName*")
            .alias(aliasName)
        val add = IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
            .index(indexName)
            .alias(aliasName)
        val request = IndicesAliasesRequest().apply {
            if (removePreviousAliases) addAliasAction(remove)
            addAliasAction(add)
        }
        LOG.info("updateAlias for alias $aliasName and pointing to $indexName ")
        return client.indices().updateAliases(request, RequestOptions.DEFAULT).isAcknowledged
    }

    fun createIndex(indexName: String): Boolean {
        val createIndexRequest = CreateIndexRequest(indexName)
            //.source(SETTINGS, XContentType.JSON)
            //.mapping(MAPPING, XContentType.JSON)
        return client.indices().create(createIndexRequest, RequestOptions.DEFAULT).isAcknowledged
    }

    fun index(docs: List<ProductDoc>): BulkResponse {
        println("indexing ${docs.size}")
        return index(docs, indexName)
    }

    fun index(doc: ProductDoc): BulkResponse {
        return index(listOf(doc), indexName)
    }

    fun index(doc: ProductDoc, indexName: String): BulkResponse {
        return index(listOf(doc), indexName)
    }

    fun index(docs: List<ProductDoc>, indexName: String): BulkResponse {
        val bulkRequest = BulkRequest()
        docs.forEach {
            println("indexing ${it.id}")
            bulkRequest.add(
                IndexRequest(indexName)
                .id(it.id.toString())
                .source(objectMapper.writeValueAsString(it), XContentType.JSON)
            )
        }
        return client.bulk(bulkRequest, RequestOptions.DEFAULT)
    }

}
