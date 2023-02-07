package no.nav.hm.grunndata.db.agreement

import no.nav.hm.grunndata.db.product.Media
import java.time.LocalDateTime
import java.util.*

data class AgreementDTO(
    val id: UUID,
    val identifier: String,
    val title: String,
    val resume: String?,
    val text: String?,
    val reference: String,
    val published: LocalDateTime,
    val expired: LocalDateTime,
    val attachments: List<AgreementAttachment> = emptyList(),
    val posts: List<AgreementPost> = emptyList(),
    val createdBy:String,
    val updatedBy: String,
    val created: LocalDateTime,
    val updated: LocalDateTime,
)

data class AgreementPost (
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