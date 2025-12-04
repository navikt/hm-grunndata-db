package no.nav.hm.grunndata.db.index.servicejob

import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.servicejob.ServiceJob
import no.nav.hm.grunndata.rapid.dto.ServiceAgreementInfo
import no.nav.hm.grunndata.rapid.dto.ServiceAttributes
import no.nav.hm.grunndata.rapid.dto.ServiceStatus
import java.time.LocalDateTime

data class ServiceJobDoc(
    override val id: String,
    val title: String,
    val supplier: Supplier,
    val supplierRef: String?,
    val hmsArtNr: String,
    val isoCategory: String,
    val published: LocalDateTime,
    val expired: LocalDateTime,
    val updated: LocalDateTime,
    val status: ServiceStatus,
    val created: LocalDateTime,
    val updatedBy: String,
    val createdBy: String,
    val attributes: ServiceAttributes,
    val agreements: List<ServiceAgreementInfo>
) : SearchDoc {
    override fun isDelete(): Boolean = status == ServiceStatus.DELETED
}

data class Supplier(val id: String, val name: String)

fun ServiceJob.toDoc(supplier: Supplier): ServiceJobDoc = ServiceJobDoc(
    id = id.toString(),
    title = title,
    supplier = supplier,
    supplierRef = supplierRef,
    hmsArtNr = hmsArtNr,
    isoCategory = isoCategory,
    published = published,
    expired = expired,
    updated = updated,
    status = status,
    created = created,
    updatedBy = updatedBy,
    createdBy = createdBy,
    attributes = attributes,
    agreements = agreements
)
