package no.nav.hm.grunndata.db.indexer

import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.agreement.Agreement
import no.nav.hm.grunndata.db.agreement.AgreementAttachment
import no.nav.hm.grunndata.db.agreement.AgreementDTO
import no.nav.hm.grunndata.db.agreement.AgreementPost
import java.time.LocalDateTime
import java.util.*

data class AgreementDoc(
    override val id: String,
    val identifier: String,
    val title: String,
    val resume: String?,
    val text: String?,
    val reference: String,
    val published: LocalDateTime,
    val expired: LocalDateTime,
    val attachments: List<AgreementAttachment>,
    val posts: List<AgreementPost>,
    val createdBy: String,
    val updatedBy: String,
    val created: LocalDateTime,
    val updated: LocalDateTime
) : SearchDoc


fun Agreement.toDoc() : AgreementDoc = AgreementDoc(
    id = id.toString(), identifier = identifier,
    title = title, resume = resume, text = text,
    reference = reference, published = published, expired = expired, attachments = attachments,
    createdBy = createdBy, updatedBy = updatedBy, created = created, updated = updated,
    posts =  posts
)

fun AgreementDTO.toDoc() : AgreementDoc = AgreementDoc (
    id = id.toString(), identifier = identifier,
    title = title, resume = resume, text = text,
    reference = reference, published = published, expired = expired, attachments = attachments,
    createdBy = createdBy, updatedBy = updatedBy, created = created, updated = updated,
    posts =  posts )