package no.nav.hm.grunndata.db.indexer


import com.fasterxml.jackson.databind.ObjectMapper
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
class Indexer(private val client: RestHighLevelClient,
              private val objectMapper: ObjectMapper) {

    companion object {
        private val LOG = LoggerFactory.getLogger(Indexer::class.java)
    }

    fun initIndex(indexName: String, settings: String?=null, mapping: String?=null) {
        if (!client.indices().exists(GetIndexRequest(indexName), RequestOptions.DEFAULT)) {
            if (createIndex(indexName, settings, mapping))
                LOG.info("$indexName has been created")
            else
                LOG.error("Failed to create $indexName")
        }
    }

    fun initAlias(aliasName: String, indexName: String) {
        val aliasIndexRequest = GetAliasesRequest(aliasName)
        val response = client.indices().getAlias(aliasIndexRequest, RequestOptions.DEFAULT)
        if (response.status() == RestStatus.NOT_FOUND) {
            LOG.warn("Alias $aliasName is not pointing to any index, updating alias")
            updateAlias(indexName, aliasName)
        }
    }

    fun updateAlias(indexName: String, aliasName: String): Boolean {
        val remove = IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
            .index("$aliasName*")
            .alias(aliasName)
        val add = IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
            .index(indexName)
            .alias(aliasName)
        val request = IndicesAliasesRequest().apply {
            addAliasAction(remove)
            addAliasAction(add)
        }
        LOG.info("updateAlias for alias $aliasName and pointing to $indexName ")
        return client.indices().updateAliases(request, RequestOptions.DEFAULT).isAcknowledged
    }

    fun createIndex(indexName: String, settings: String?=null, mapping: String?=null): Boolean {
        val createIndexRequest = CreateIndexRequest(indexName)
        settings?.let { createIndexRequest.source(settings, XContentType.JSON) }
        mapping?.let { createIndexRequest.mapping(mapping, XContentType.JSON) }
        return client.indices().create(createIndexRequest, RequestOptions.DEFAULT).isAcknowledged
    }

    fun index(doc: SearchDoc, indexName: String): BulkResponse {
        return index(listOf(doc), indexName)
    }

    fun index(docs: List<SearchDoc>, indexName: String): BulkResponse {
        val bulkRequest = BulkRequest()
        docs.forEach {
            LOG.debug("indexing ${it.id}")
            bulkRequest.add(
                IndexRequest(indexName)
                    .id(it.id)
                    .source(objectMapper.writeValueAsString(it), XContentType.JSON)
            )
        }
        return client.bulk(bulkRequest, RequestOptions.DEFAULT)
    }

    fun indexExists(indexName: String):Boolean = client.indices()
        .exists(GetIndexRequest(indexName), RequestOptions.DEFAULT)

}
