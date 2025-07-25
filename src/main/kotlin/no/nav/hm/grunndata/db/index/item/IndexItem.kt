package no.nav.hm.grunndata.db.index.item

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.LocalDateTime
import java.util.UUID

@MappedEntity("index_item")
data class IndexItem(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val oid: String,
    val delete: Boolean = false,
    val payload: String,
    val indexType: IndexType,
    val status: IndexItemStatus = IndexItemStatus.PENDING,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
)

enum class IndexItemStatus {
    PENDING, DONE, ERROR
}

enum class IndexType {
    NEWS, PRODUCT, AGREEMENT, SUPPLIER, EXTERNAL_PRODUCT
}