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
    val createdBy: String = HMDB,
    val updatedBy: String = HMDB,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)


