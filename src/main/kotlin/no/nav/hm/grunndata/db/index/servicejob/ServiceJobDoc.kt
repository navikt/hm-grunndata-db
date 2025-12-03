package no.nav.hm.grunndata.db.index.servicejob

import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.servicejob.ServiceJob
import no.nav.hm.grunndata.rapid.dto.ServiceStatus
import java.time.LocalDateTime

data class ServiceJobDoc(
    override val id: String,
    val title: String,
    val supplierId: String,
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
    val attributes: Any,
    val agreements: Any
) : SearchDoc {
    override fun isDelete(): Boolean = status == ServiceStatus.DELETED
}

fun ServiceJob.toDoc(): ServiceJobDoc = ServiceJobDoc(
    id = id.toString(),
    title = title,
    supplierId = supplierId.toString(),
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
