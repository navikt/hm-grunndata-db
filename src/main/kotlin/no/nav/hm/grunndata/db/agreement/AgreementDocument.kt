package no.nav.hm.grunndata.db.agreement

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.LocalDate
import java.time.LocalDateTime

@MappedEntity
data class AgreementDocument(
    val id: Long,
    val title: String,
    val resume: String?,
    val text: String?,
    val link: String?,
    val type: Int,
    val publish: LocalDateTime,
    val expire: LocalDateTime?,
    val externid: String?,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)

@MappedEntity
data class AgreementPost(
    val id: Long=-1L,
    val agreementId: Long,
    val nr: Int,
    val title: String,
    val desc: String,
    val created: LocalDateTime = LocalDateTime.now()
)

@MappedEntity("product_agreement_v1")
data class ProductAgreement (
    @field:Id
    val id: Long = -1L,
    val rank: Int,
    val postId: Long
)

data class AgreementDTO (
    val id: String,
    val publish: LocalDate?,
    val expire: LocalDate?,
    val rank: Long=1,
    val postId: String,
    val postNr: String,
    val postTitle: String,
)