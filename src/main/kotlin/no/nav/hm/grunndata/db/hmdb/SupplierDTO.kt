package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierInfo
import java.time.LocalDateTime

data class SupplierDTO (
    val adressid: Long,
    val adressnamn1: String?,
    val postadress1: String?,
    val postnr: String?,
    val postort: String?,
    val telefon: String?,
    val epost: String?,
    val www: String?,
    val landkod: String?,
    val lastupdated: LocalDateTime,
    val adrinsertdate: LocalDateTime?,
)

fun SupplierDTO.toSupplier() = Supplier(id= adressid, hmdbId = adressid, name = adressnamn1!!, info = SupplierInfo(
        address = postadress1, email = epost, phone = telefon, homepage = www))