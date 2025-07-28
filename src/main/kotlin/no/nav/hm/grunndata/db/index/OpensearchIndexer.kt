package no.nav.hm.grunndata.db.index


import jakarta.inject.Singleton
import java.io.StringReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch._types.Refresh
import org.opensearch.client.opensearch._types.mapping.TypeMapping
import org.opensearch.client.opensearch.core.BulkRequest
import org.opensearch.client.opensearch.core.BulkResponse
import org.opensearch.client.opensearch.core.CountRequest
import org.opensearch.client.opensearch.core.DeleteRequest
import org.opensearch.client.opensearch.core.DeleteResponse
import org.opensearch.client.opensearch.core.bulk.BulkOperation
import org.opensearch.client.opensearch.core.bulk.DeleteOperation
import org.opensearch.client.opensearch.core.bulk.IndexOperation
import org.opensearch.client.opensearch.indices.CreateIndexRequest
import org.opensearch.client.opensearch.indices.ExistsAliasRequest
import org.opensearch.client.opensearch.indices.ExistsRequest
import org.opensearch.client.opensearch.indices.GetAliasRequest
import org.opensearch.client.opensearch.indices.IndexSettings
import org.opensearch.client.opensearch.indices.UpdateAliasesRequest
import org.opensearch.client.opensearch.indices.update_aliases.ActionBuilders
import org.slf4j.LoggerFactory

@Singleton
class OpensearchIndexer(private val client: OpenSearchClient) {

    companion object {
        private val LOG = LoggerFactory.getLogger(OpensearchIndexer::class.java)
    }

    fun updateAlias(aliasName: String, indexName: String): Boolean {
        val updateAliasesRequestBuilder = UpdateAliasesRequest.Builder()
        if (existsAlias(aliasName)) {
            val aliasResponse = getAlias(aliasName)
            val indices = aliasResponse.result().keys
            indices.forEach { index ->
                val removeAction = ActionBuilders.remove().index(index).alias(aliasName).build()
                updateAliasesRequestBuilder.actions { it.remove(removeAction) }
            }
        }
        val addAction = ActionBuilders.add().index(indexName).alias(aliasName).build()
        updateAliasesRequestBuilder.actions { it.add(addAction) }
        val updateAliasesRequest = updateAliasesRequestBuilder.build()
        val ack = client.indices().updateAliases(updateAliasesRequest).acknowledged()
        LOG.info("update for alias $aliasName and pointing to $indexName with status: $ack")
        return ack
    }


    fun existsAlias(aliasName: String)
        = client.indices().existsAlias(ExistsAliasRequest.Builder().name(aliasName).build()).value()

    fun getAlias(aliasName: String)
        = client.indices().getAlias(GetAliasRequest.Builder().name(aliasName).build())

    fun createIndex(indexName: String, settings: String, mapping: String): Boolean {
        val mapper = client._transport().jsonpMapper()
        val createIndexRequest = CreateIndexRequest.Builder().index(indexName)
        val settingsParser = mapper.jsonProvider().createParser(StringReader(settings))
        val indexSettings = IndexSettings._DESERIALIZER.deserialize(settingsParser, mapper)
        createIndexRequest.settings(indexSettings)
        val mappingsParser = mapper.jsonProvider().createParser(StringReader(mapping))
        val typeMapping = TypeMapping._DESERIALIZER.deserialize(mappingsParser, mapper)
        createIndexRequest.mappings(typeMapping)
        val ack = client.indices().create(createIndexRequest.build()).acknowledged()!!
        LOG.info("Created $indexName with status: $ack")
        return ack
    }

    fun indexDoc(docs: List<IndexDoc>): BulkResponse {
        val operations = docs.map { document ->
            if (document.delete) {
                LOG.info("deleting document ${document.id} from index ${document.indexName}")
                BulkOperation.Builder().delete(
                    DeleteOperation.of { it.index(document.indexName).id(document.id.toString()) }
                ).build()
            }
            else BulkOperation.Builder().index(
                IndexOperation.of { it.index(document.indexName).id(document.id.toString()).document(document.doc) }
            ).build()
        }
        val bulkRequest = BulkRequest.Builder()
            .operations(operations)
            .refresh(Refresh.WaitFor)
            .build()
        return try {
            client.bulk(bulkRequest)
        }
        catch (e: Exception) {
            LOG.error("Failed to bulk index ${docs.size}", e)
            throw e
        }
    }

//    fun index(doc: SearchDoc, indexName: String): BulkResponse {
//        return index(listOf(doc), indexName)
//    }
//
//    fun index(docs: List<SearchDoc>, indexName: String): BulkResponse {
//        val operations = docs.map { document ->
//            BulkOperation.Builder().index(
//                IndexOperation.of { it.index(indexName).id(document.id).document(document) }
//            ).build()
//        }
//        val bulkRequest = BulkRequest.Builder()
//            .index(indexName)
//            .operations(operations)
//            .refresh(Refresh.WaitFor)
//            .build()
//        return try {
//            client.bulk(bulkRequest)
//        }
//        catch (e: Exception) {
//            LOG.error("Failed to index $docs to $indexName", e)
//            throw e
//        }
//    }

    fun delete(id: String, indexName: String): DeleteResponse {
        val request = DeleteRequest.Builder().index(indexName).id(id)
        return client.delete(request.build())
    }

    fun indexExists(indexName: String):Boolean =
        client.indices().exists(ExistsRequest.Builder().index(indexName).build()).value()

    fun docCount(aliasName:String): Long = client.count(CountRequest.Builder().index(aliasName).build()).count()

    fun initAlias(aliasName: String, settings: String, mapping: String) {
        if (!existsAlias(aliasName)) {
            LOG.warn("alias $aliasName is not pointing any index")
            val indexName = createIndexName(aliasName)
            LOG.info("Creating index $indexName")
            createIndex(indexName,settings, mapping)
            updateAlias(aliasName, indexName)
        }
        else {
            LOG.info("Aliases is pointing to ${getAlias(aliasName).toJsonString()}")
        }
    }
}

fun createIndexName(aliasIndexName: String): String = "${aliasIndexName}_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))}"
