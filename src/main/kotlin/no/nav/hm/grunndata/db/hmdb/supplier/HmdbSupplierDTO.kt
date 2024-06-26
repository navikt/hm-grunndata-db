package no.nav.hm.grunndata.db.hmdb.supplier

import no.nav.hm.grunndata.db.hmdb.product.HmDbIdentifier
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.rapid.dto.SupplierInfo
import no.nav.hm.grunndata.rapid.dto.SupplierStatus
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

fun HmdbSupplierDTO.toSupplier() = Supplier(identifier = "$adressid".HmDbIdentifier(), name = adressnamn1!!,
    created = adrinsertdate ?: LocalDateTime.now(), updated = lastupdated, status = SupplierStatus.ACTIVE,info = SupplierInfo(
        address = postadress1, email = epost, phone = telefon, homepage = www, postNr = postnr, postLocation = postort, countryCode = landkod?.trim())
)