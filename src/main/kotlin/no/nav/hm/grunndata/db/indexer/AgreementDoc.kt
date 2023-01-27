package no.nav.hm.grunndata.db.indexer

import no.nav.hm.grunndata.db.agreement.Agreement
import no.nav.hm.grunndata.db.agreement.AgreementDTO
import no.nav.hm.grunndata.db.agreement.AgreementPost
import java.time.LocalDateTime

data class AgreementDoc(
    override val id: String,
    val identifier: String,
    val title: String,
    val resume: String?,
    val text: String?,
    val reference: String,
    val publish: LocalDateTime,
    val expire: LocalDateTime?,
    val createdBy: String,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val posts: List<AgreementPost>
) : SearchDoc


fun Agreement.toDoc() : AgreementDoc = AgreementDoc(
    id = id.toString(), identifier = identifier,
    title = title, resume = resume, text = text,
    reference = reference, publish = published, expire = expired,
    createdBy = createdBy, created = created, updated = updated,
    posts =  posts
)

fun AgreementDTO.toDoc() : AgreementDoc = AgreementDoc (
    id = id.toString(), identifier = identifier,
    title = title, resume = resume, text = text,
    reference = reference, publish = published, expire = expired,
    createdBy = createdBy, created = created, updated = updated,
    posts =  posts )