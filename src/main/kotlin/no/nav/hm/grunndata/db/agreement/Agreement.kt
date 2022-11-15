package no.nav.hm.grunndata.db.agreement

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import no.nav.hm.grunndata.db.HMDB
import java.time.LocalDateTime

@MappedEntity("agreement_v1")
data class Agreement (
    @field:Id
    val id: Long=-1L,
    val identifier: String,
    val title: String,
    val resume: String?,
    val text: String?,
    val link: String?,
    val publish: LocalDateTime,
    val expire: LocalDateTime?,
    val createdBy: String = HMDB,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)

@MappedEntity("agreement_post_v1")
data class AgreementPost (
    @field:Id
    val id: Long=-1L,
    val agreementId: Long=-1L,
    val identifier: String,
    val nr: Int,
    val title: String,
    val desc: String,
    val createdBy: String = HMDB,
    val created: LocalDateTime = LocalDateTime.now()
)

