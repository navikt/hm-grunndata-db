package no.nav.hm.grunndata.db.index

import com.fasterxml.jackson.databind.JsonNode
import java.util.UUID


interface SearchDoc {
    val id: String
    fun isDelete(): Boolean
}

data class IndexDoc(
    val id: UUID,
    val delete: Boolean = false,
    val doc: JsonNode,
    val indexName: String
)