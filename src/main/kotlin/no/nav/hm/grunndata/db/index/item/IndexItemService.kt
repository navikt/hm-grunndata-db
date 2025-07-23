package no.nav.hm.grunndata.db.index.item

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.hm.grunndata.db.index.SearchDoc

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

}

enum class IndexType {
    NEWS, PRODUCT, AGREEMENT, SUPPLIER
}