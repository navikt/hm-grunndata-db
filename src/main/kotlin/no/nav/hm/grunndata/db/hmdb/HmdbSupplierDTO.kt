package no.nav.hm.grunndata.db.hmdb

import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierInfo
import java.time.LocalDateTime

data class HmdbSupplierDTO (
    val adressid: Long, // this is id in hmdb
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

fun HmdbSupplierDTO.toSupplier() = Supplier(identifier = "hmdbid-$adressid", name = adressnamn1!!, info = SupplierInfo(
        address = postadress1, email = epost, phone = telefon, homepage = www))