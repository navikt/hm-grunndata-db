package no.nav.hm.grunndata.db.servicejob

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import no.nav.hm.grunndata.rapid.dto.ServiceAgreementInfo
import no.nav.hm.grunndata.rapid.dto.ServiceAttributes
import no.nav.hm.grunndata.rapid.dto.ServiceStatus
import java.time.LocalDateTime
import java.util.UUID

@MappedEntity("service_job_v1")
data class ServiceJob (
    @field:Id
    val id: UUID,
    val title: String,
    val supplierId: UUID,
    val supplierRef: String?,
    val hmsArtNr: String,
    val isoCategory: String,
    val published: LocalDateTime,
    val expired: LocalDateTime,
    val updated: LocalDateTime = LocalDateTime.now(),
    val status: ServiceStatus = ServiceStatus.ACTIVE,
    val created: LocalDateTime = LocalDateTime.now(),
    val updatedBy: String,
    val createdBy: String,
    @field:TypeDef(type = DataType.JSON)
    val attributes: ServiceAttributes,
    @field:TypeDef(type = DataType.JSON)
    val agreements: List<ServiceAgreementInfo> = emptyList(),
)

