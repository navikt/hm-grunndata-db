package no.nav.hm.grunndata.db.techlabel

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import java.time.LocalDateTime
import java.util.*
import no.nav.hm.grunndata.db.REGISTER

@MappedEntity("techlabel_v1")
data class TechLabel(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val identifier: String = UUID.randomUUID().toString(),
    val label: String,
    val guide: String,
    val definition: String? = null,
    val isocode: String,
    val type: String,
    val unit: String?,
    val sort: Int = 1,
    @field:TypeDef(type = DataType.JSON)
    val options: List<String> = emptyList(),
    val createdBy: String = REGISTER,
    val updatedBy: String = REGISTER,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)



