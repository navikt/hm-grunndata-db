package no.nav.hm.grunndata.db.index.item

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.SearchDoc
import java.time.LocalDateTime

@Singleton
open class IndexItemService(
    private val indexItemRepository: IndexItemRepository,
    private val indexer: OpensearchIndexer,
    private val objectMapper: ObjectMapper
) {

    var indexItemSupport: Map<IndexType, IndexItemSupport> = emptyMap()

    suspend fun saveIndexItem(doc: SearchDoc, indexType: IndexType): IndexItem {
        val indexItem = IndexItem(
            oid = doc.id,
            delete = doc.isDelete(),
            payload = objectMapper.writer().writeValueAsString(doc),
            indexType = indexType,
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
        val indexDocs = items.map { item ->
            val support = indexItemSupport[item.indexType]
                ?: throw IllegalStateException("No IndexItemSupport found for type ${item.indexType}")
            IndexDoc(
                id = item.oid,
                indexType = item.indexType,
                doc = objectMapper.readValue(item.payload, support.getSearchDocClassType()),
                indexName = support.getAliasIndexName()
            )
        }
        indexer.indexDoc(indexDocs)
        items.forEach {
            indexItemRepository.update(it.copy(status = IndexItemStatus.DONE, updated = LocalDateTime.now()))
        }
    }

}


