package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.LocalDateTime

@MappedEntity
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
    val adrinsertdate: LocalDateTime,
)
