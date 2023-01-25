package no.nav.hm.grunndata.db.agreement

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.product.Media
import java.time.LocalDateTime
import java.util.*
import javax.management.monitor.StringMonitor

@MappedEntity("agreement_v1")
data class Agreement (
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val identifier: String,
    val title: String,
    val resume: String?,
    val text: String?,
    val reference: String,
    val publish: LocalDateTime,
    val expire: LocalDateTime?,
    @field:TypeDef(type = DataType.JSON)
    val attachments: List<AgreementAttachment> = emptyList(),
    val createdBy: String = HMDB,
    val updatedBy: String = HMDB,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)

@MappedEntity("agreement_post_v1")
data class AgreementPost (
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val agreementId: UUID,
    val identifier: String,
    val nr: Int,
    val title: String,
    val description: String,
    val created: LocalDateTime = LocalDateTime.now()
)

data class AgreementAttachment (
    val title: String?,
    val media: List<Media> = emptyList(),
    val description: String?,
)

data class AgreementDTO(
    val id: UUID,
    val identifier: String,
    val title: String,
    val resume: String?,
    val text: String?,
    val reference: String,
    val publish: LocalDateTime,
    val expire: LocalDateTime?,
    val attachments: List<AgreementAttachment> = emptyList(),
    val createdBy:String,
    val updatedBy: String,
    val created: LocalDateTime,
    val updated: LocalDateTime,
)

data class AgreementPostDTO (
    val id: UUID,
    val agreementId: UUID,
    val identifier: String,
    val nr: Int,
    val title: String,
    val description: String,
    val created: LocalDateTime,
)

fun Agreement.toDTO(): AgreementDTO = AgreementDTO(
    id = id, identifier = identifier, title = title, resume = resume, text = text, reference=reference,
    publish = publish, expire = expire, attachments = attachments, createdBy = createdBy, updatedBy = updatedBy, created = created,
    updated = updated )

fun AgreementPost.toDTO(): AgreementPostDTO = AgreementPostDTO(
    id = id, agreementId = agreementId, identifier = identifier, nr = nr, title = title, description = description,
    created = created )
