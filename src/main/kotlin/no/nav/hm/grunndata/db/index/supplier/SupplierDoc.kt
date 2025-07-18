package no.nav.hm.grunndata.db.index.supplier

import no.nav.hm.grunndata.rapid.dto.SupplierDTO
import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.rapid.dto.SupplierStatus
import java.time.LocalDateTime

data class SupplierDoc(
    override val id: String,
    val identifier: String,
    val status: SupplierStatus,
    val name: String,
    val address: String?,
    val postNr: String?,
    val postLocation: String?,
    val countryCode: String?,
    val email: String?,
    val phone: String?,
    val homepage: String?,
    val createdBy: String,
    val updatedBy: String,
    val created: LocalDateTime,
    val updated: LocalDateTime) : SearchDoc

fun SupplierDTO.toDoc(): SupplierDoc = SupplierDoc(
    id = id.toString(), identifier = identifier, status = status, name = name, address = info.address, postNr = info.postNr,
    postLocation = info.postLocation, countryCode = info.countryCode, email = info.email,
    phone = info.phone, homepage = info.homepage, createdBy = createdBy, updatedBy = updatedBy,
    created = created, updated = updated
)