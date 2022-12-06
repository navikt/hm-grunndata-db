package no.nav.hm.grunndata.db.indexer

import no.nav.hm.grunndata.db.agreement.AgreementDocument
import java.time.LocalDateTime
import java.util.UUID

data class AgreementDoc(
    override val id: String,
    val identifier: String,
    val title: String,
    val resume: String?,
    val text: String?,
    val link: String?,
    val reference: String,
    val publish: LocalDateTime,
    val expire: LocalDateTime?,
    val createdBy: String,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val posts: List<AgreementPostDoc>
) : SearchDoc

data class AgreementPostDoc (
    val id: UUID,
    val identifier: String,
    val nr: Int,
    val title: String,
    val description: String,
    val created: LocalDateTime
)

fun AgreementDocument.toDoc() : AgreementDoc = AgreementDoc(
    id = agreement.id.toString(), identifier = agreement.identifier,
    title = agreement.title, resume = agreement.resume, text = agreement.text,
    link = agreement.link, reference = agreement.reference, publish = agreement.publish, expire = agreement.expire,
    createdBy = agreement.createdBy, created = agreement.created, updated = agreement.updated,
    posts = agreementPost.map { AgreementPostDoc(
        id = it.id, identifier = it.identifier, nr = it.nr, title = it.title, description = it.description,
        created = it.created
    ) }
)