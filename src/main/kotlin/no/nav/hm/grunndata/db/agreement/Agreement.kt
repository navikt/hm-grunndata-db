package no.nav.hm.grunndata.db.agreement

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import no.nav.hm.grunndata.db.HMDB
import java.time.LocalDateTime
import java.util.*

@MappedEntity("agreement_v1")
data class Agreement (
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val identifier: String,
    val title: String,
    val resume: String?,
    val text: String?,
    val link: String?,
    val reference: String,
    val publish: LocalDateTime,
    val expire: LocalDateTime?,

    val documents: List<AttachmentDoc> = emptyList(),
    val createdBy: String = HMDB,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)

@MappedEntity("agreement_post_v1")
data class AgreementPost (
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val agreementId: UUID = UUID.randomUUID(),
    val identifier: String,
    val nr: Int,
    val title: String,
    val description: String,
    val createdBy: String = HMDB,
    val created: LocalDateTime = LocalDateTime.now()
)

