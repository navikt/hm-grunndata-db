package no.nav.hm.grunndata.db.index

import com.fasterxml.jackson.databind.JsonNode
import no.nav.hm.grunndata.db.index.item.IndexType
import java.util.UUID


interface SearchDoc {
    val id: String
    fun isDelete(): Boolean
}

data class IndexDoc(
    val id: UUID,
    val indexType: IndexType,
    val doc: SearchDoc,
    val delete: Boolean = doc.isDelete(),
    val indexName: String
)