package no.nav.hm.grunndata.db.agreement

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.rapid.dto.AgreementAttachment
import no.nav.hm.grunndata.rapid.dto.AgreementDTO
import no.nav.hm.grunndata.rapid.dto.AgreementPost
import no.nav.hm.grunndata.rapid.dto.AgreementStatus
import java.time.LocalDateTime
import java.util.*



@MappedEntity("agreement_v1")
data class Agreement (
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val identifier: String,
    val title: String,
    val status: AgreementStatus = AgreementStatus.ACTIVE,
    val resume: String?,
    val text: String?,
    val reference: String,
    val published: LocalDateTime,
    val expired: LocalDateTime,
    @field:TypeDef(type = DataType.JSON)
    val attachments: List<AgreementAttachment> = emptyList(),
    @field:TypeDef(type = DataType.JSON)
    val posts: List<AgreementPost> = emptyList(),
    @field: TypeDef(type = DataType.JSON)
    val isoCategory: List<String> = emptyList(),
    val createdBy: String = HMDB,
    val updatedBy: String = HMDB,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val previousAgreement: UUID? = null
)

fun Agreement.toDTO(): AgreementDTO = AgreementDTO(
    id = id, identifier = identifier, status = status, title = title, resume = resume, text = text, reference=reference,
    published = published, expired = expired, attachments = attachments, createdBy = createdBy, updatedBy = updatedBy, created = created,
    updated = updated, posts = posts, isoCategory = isoCategory, previousAgreement = previousAgreement)

fun AgreementDTO.toEntity(): Agreement = Agreement(
    id = id, identifier = identifier, title = title, status = status, resume = resume, text = text, reference = reference,
    published = published, expired = expired, attachments = attachments, posts = posts, createdBy = createdBy,
    updatedBy = updatedBy, created = created, isoCategory = isoCategory, updated = updated, previousAgreement = previousAgreement
)

