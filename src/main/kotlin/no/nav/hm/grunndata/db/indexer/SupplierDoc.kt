package no.nav.hm.grunndata.db.indexer

import no.nav.hm.grunndata.db.supplier.Supplier
import java.time.LocalDateTime
import java.util.*

data class SupplierDoc(
    override val id: String,
    val identifier: String,
    val name: String,
    val address: String?,
    val email: String?,
    val phone: String?,
    val homepage: String?,
    val createdBy: String,
    val updatedBy: String,
    val created: LocalDateTime,
    val updated: LocalDateTime) : SearchDoc

fun Supplier.toDoc(): SupplierDoc = SupplierDoc (
    id = id.toString(), identifier = identifier, name = name, address = info.address, email = info.email,
    phone = info.phone, homepage = info.homepage, createdBy = createdBy, updatedBy = updatedBy,
    created = created, updated = updated)
