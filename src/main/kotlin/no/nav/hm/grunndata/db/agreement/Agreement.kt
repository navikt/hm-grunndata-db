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
    val createdBy: String = HMDB,
    val created: LocalDateTime = LocalDateTime.now()
)

data class AgreementAttachment (
    val title: String,
    val media: List<Media> = emptyList(),
    val description: String?,
)