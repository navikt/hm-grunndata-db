package no.nav.hm.grunndata.db.index.agreement

import no.nav.hm.grunndata.rapid.dto.AgreementAttachment
import no.nav.hm.grunndata.rapid.dto.AgreementDTO
import no.nav.hm.grunndata.rapid.dto.AgreementPost
import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.rapid.dto.AgreementStatus
import java.time.LocalDateTime
import java.util.UUID

data class AgreementDoc(
    override val id: String,
    val identifier: String,
    val title: String,
    val label: String,
    val status: AgreementStatus,
    val resume: String?,
    val text: String?,
    val reference: String,
    val published: LocalDateTime,
    val expired: LocalDateTime,
    val attachments: List<AgreementAttachment>,
    val posts: List<AgreementPost>,
    val isoCategory: List<String>,
    val createdBy: String,
    val updatedBy: String,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val previousAgreement: UUID? = null,
) : SearchDoc {
        override fun isDelete(): Boolean = status == AgreementStatus.DELETED
    }


fun AgreementDTO.toDoc() : AgreementDoc = AgreementDoc (
    id = id.toString(), identifier = identifier, status = status,
    title = title, label = AgreementLabels.Companion.matchTitleToLabel(title), resume = resume, text = text,
    reference = reference, published = published, expired = expired, attachments = attachments,
    createdBy = createdBy, updatedBy = updatedBy, created = created, updated = updated,
    posts =  posts, isoCategory = isoCategory, previousAgreement = previousAgreement)
