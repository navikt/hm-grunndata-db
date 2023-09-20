package no.nav.hm.grunndata.db.series

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import no.nav.hm.grunndata.rapid.dto.SeriesStatus
import java.time.LocalDateTime
import java.util.*

@MappedEntity("series_v1")
data class Series(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val supplierId: UUID,
    val status: SeriesStatus = SeriesStatus.ACTIVE,
    val name: String,
    val identifier: String,
    val createdBy: String,
    val updatedBy: String,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)

