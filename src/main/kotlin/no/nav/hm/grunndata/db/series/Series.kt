package no.nav.hm.grunndata.db.series

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import no.nav.hm.grunndata.rapid.dto.SeriesRapidDTO
import no.nav.hm.grunndata.rapid.dto.SeriesRegistrationRapidDTO
import no.nav.hm.grunndata.rapid.dto.SeriesStatus
import java.time.LocalDateTime
import java.util.*

@MappedEntity("series_v1")
data class Series(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val supplierId: UUID,
    val status: SeriesStatus = SeriesStatus.ACTIVE,
    val title: String,
    val text: String,
    val identifier: String,
    val createdBy: String,
    val updatedBy: String,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val expired: LocalDateTime = LocalDateTime.now()
)


fun Series.toRapidDTO() = SeriesRapidDTO (id = id, status = status, title = title,
    text = text,
    supplierId = supplierId, expired = expired,
    identifier = identifier, createdBy = createdBy,
    updatedBy = updatedBy, created = created, updated = updated)

fun SeriesRegistrationRapidDTO.toEntity() = Series(
    id = id,
    supplierId = supplierId,
    status = status,
    title = title,
    identifier = identifier,
    createdBy = createdBy,
    updatedBy = updatedBy,
    created = created,
    updated = updated,
    expired = expired,
    text = text,
)