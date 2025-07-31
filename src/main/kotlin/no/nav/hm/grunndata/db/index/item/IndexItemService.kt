package no.nav.hm.grunndata.db.index.item

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.SearchDoc
import java.time.Duration
import java.time.LocalDateTime

@Singleton
open class IndexItemService(
    private val indexItemRepository: IndexItemRepository,
    private val indexer: OpensearchIndexer,
    private val indexSettings: IndexSettings,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(IndexItemService::class.java)
    }

    suspend fun saveIndexItem(doc: SearchDoc, indexType: IndexType, indexName: String): IndexItem {
        val config = indexSettings.indexConfigMap[indexType]
        if (config == null) {
            throw IllegalArgumentException("No index configuration found for index type: $indexType")
        }
        val indexItem = IndexItem(
            oid = doc.id,
            delete = doc.isDelete(),
            payload = objectMapper.writer().writeValueAsString(doc),
            indexType = indexType,
            indexName = indexName,
            status = IndexItemStatus.PENDING
        )
        return if (config.enabled) { indexItemRepository.save(indexItem) } else indexItem
    }

    suspend fun saveIndexItem(doc: SearchDoc, indexType: IndexType): IndexItem {
        val indexName = indexSettings.indexConfigMap[indexType]?.aliasIndexName
            ?: throw IllegalArgumentException("No index name found for index type: $indexType")
        return saveIndexItem(doc, indexType, indexName)
    }

    @Transactional
    open suspend fun processPendingIndexItems(size: Int): Int {
        val items = indexItemRepository.findAndLockForProcessing(status = IndexItemStatus.PENDING, limit = size)
        if (items.isEmpty()) {
            return 0
        }
        val uniqueItems = items.groupBy { it.oid to it.indexType }.map { it.value.last() }
        LOG.info("Indexing ${items.size} with ${uniqueItems.size} unique items")
        indexer.indexItems(uniqueItems)
        items.forEach {
            indexItemRepository.update(it.copy(status = IndexItemStatus.DONE, updated = LocalDateTime.now()))
        }
        return items.size
    }

    @Transactional
    open suspend fun deleteOldIndexItems(duration: Duration): Long {
        LOG.info("Deleting old index items $duration")
        val deleted = indexItemRepository.deleteByStatusAndUpdatedBefore(IndexItemStatus.DONE, LocalDateTime.now().minusDays(duration.toDays()))
        LOG.info("Deleted ${deleted} old index items")
        return deleted
    }


}


