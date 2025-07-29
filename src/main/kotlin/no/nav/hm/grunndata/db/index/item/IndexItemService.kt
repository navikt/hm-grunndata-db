package no.nav.hm.grunndata.db.index.item

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.SearchDoc
import java.time.LocalDateTime

@Singleton
open class IndexItemService(
    private val indexItemRepository: IndexItemRepository,
    private val indexer: OpensearchIndexer,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(IndexItemService::class.java)
    }

    suspend fun saveIndexItem(doc: SearchDoc, indexType: IndexType, indexName: String): IndexItem {
        val indexItem = IndexItem(
            oid = doc.id,
            delete = doc.isDelete(),
            payload = objectMapper.writer().writeValueAsString(doc),
            indexType = indexType,
            indexName = indexName,
            status = IndexItemStatus.PENDING
        )
        return indexItemRepository.save(indexItem)
    }

    @Transactional
    open suspend fun processPendingIndexItems(size: Int) {
        val items = indexItemRepository.findAndLockForProcessing(status = IndexItemStatus.PENDING, limit = size)
        if (items.isEmpty()) {
            return
        }
        val uniqueItems = items.groupBy { it.oid to it.indexType }.map { it.value.last() }
        LOG.info("Indexing ${items.size} with ${uniqueItems.size} unique items")
        indexer.indexItems(uniqueItems)
        items.forEach {
            indexItemRepository.update(it.copy(status = IndexItemStatus.DONE, updated = LocalDateTime.now()))
        }
    }


}


