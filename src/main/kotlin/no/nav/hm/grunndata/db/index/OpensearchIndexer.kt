package no.nav.hm.grunndata.db.index


import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.item.IndexItem
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch._types.Refresh
import org.opensearch.client.opensearch._types.mapping.TypeMapping
import org.opensearch.client.opensearch.core.*
import org.opensearch.client.opensearch.core.bulk.BulkOperation
import org.opensearch.client.opensearch.core.bulk.DeleteOperation
import org.opensearch.client.opensearch.core.bulk.IndexOperation
import org.opensearch.client.opensearch.indices.*
import org.opensearch.client.opensearch.indices.ExistsRequest
import org.opensearch.client.opensearch.indices.update_aliases.ActionBuilders
import org.slf4j.LoggerFactory
import java.io.StringReader

@Singleton
class OpensearchIndexer(private val client: OpenSearchClient, private val objectMapper: ObjectMapper) {

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

    fun indexItems(items: List<IndexItem>): BulkResponse {
        val operations = items.map { item ->
            if (item.delete) {
                BulkOperation.Builder().delete(
                    DeleteOperation.of { it.index(item.indexName).id(item.oid) }
                ).build()
            } else {
                val docMap: Map<String, Any> = objectMapper.readValue(item.payload, Map::class.java) as Map<String, Any>
                BulkOperation.Builder().index(
                    IndexOperation.of { it.index(item.indexName).id(item.oid).document(docMap) }
                ).build()
            }
        }
        val bulkRequest = BulkRequest.Builder()
            .operations(operations)
            .refresh(Refresh.WaitFor)
            .build()
        return try {
            client.bulk(bulkRequest)
        } catch (e: Exception) {
            LOG.error("Failed to bulk index ${items.size}", e)
            throw e
        }
    }

    fun indexDoc(docs: List<IndexDoc>): BulkResponse {
        val operations = docs.map { document ->
            if (document.delete) {
                LOG.info("deleting document ${document.id} from index ${document.indexName}")
                BulkOperation.Builder().delete(
                    DeleteOperation.of { it.index(document.indexName).id(document.id) }
                ).build()
            }
            else {

                BulkOperation.Builder().index(
                IndexOperation.of { it.index(document.indexName).id(document.id).document(document.doc) }
            ).build() }
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

    fun delete(id: String, indexName: String): DeleteResponse {
        val request = DeleteRequest.Builder().index(indexName).id(id)
        return client.delete(request.build())
    }

    fun indexExists(indexName: String):Boolean =
        client.indices().exists(ExistsRequest.Builder().index(indexName).build()).value()

    fun docCount(aliasName:String): Long = client.count(CountRequest.Builder().index(aliasName).build()).count()

    fun checkAliasIsPointingToIndex(aliasName: String) {
        if (!existsAlias(aliasName)) {
            LOG.error("alias $aliasName is not pointing to any index")
            LOG.error("Remember to create index with correct settings and mappings and point alias to it")
        }
        else {
            LOG.info("Aliases is pointing to ${getAlias(aliasName).toJsonString()}")
        }
    }
}


