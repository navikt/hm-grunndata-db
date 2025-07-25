package no.nav.hm.grunndata.db.index.item

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort.Order
import io.micronaut.data.model.Sort.of
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.SearchDoc

@Singleton
class IndexItemService(private val indexItemRepository: IndexItemRepository, private val objectMapper: ObjectMapper) {


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

    suspend fun findByOid(oid: String): IndexItem? {
        return indexItemRepository.findByOid(oid)
    }

    suspend fun findIndexItemsByType(indexType: IndexType): List<IndexItem> {
        return indexItemRepository.findByIndexType(indexType)
    }

    suspend fun retrievePendingIndexItems(size: Int): List<IndexItem> {
        return indexItemRepository.findByStatusOrderByUpdatedAsc(IndexItemStatus.PENDING, Pageable.from(0, size, of(
            Order.asc("updated"))
        ))
    }

}


