package no.nav.hm.grunndata.db.techlabel

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import no.nav.hm.grunndata.db.HMDB
import java.time.LocalDateTime
import java.util.*

@MappedEntity("techlabel_v1")
data class TechLabel(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val identifier: String = UUID.randomUUID().toString(),
    val label: String,
    val guide: String,
    val isocode: String,
    val type: String,
    val unit: String?,
    val sort: Int = 1,
    val createdBy: String = HMDB,
    val updatedBy: String = HMDB,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)

data class TechLabelDTO(
    val id: UUID,
    val identifier: String,
    val label: String,
    val guide: String,
    val isocode: String,
    val type: String,
    val unit: String?,
    val sort: Int,
    val createdBy: String = HMDB,
    val updatedBy: String = HMDB,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)

fun TechLabel.toDTO(): TechLabelDTO = TechLabelDTO(id = id, identifier = identifier, label = label, guide = guide,
    isocode = isocode, type = type, unit = unit, sort = sort, createdBy = createdBy, updatedBy = updatedBy,
    created = created, updated = updated
)


