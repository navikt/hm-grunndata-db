package no.nav.hm.grunndata.db.hmdb

import java.time.LocalDateTime

data class LeverandorDTO(
    val leverandorid: String,
    val leverandornavn: String?,
    val adresse: String?,
    val postnummer: String?,
    val poststed: String?,
    val telefon: String?,
    val epost: String?,
    val www: String?,
    val landkode: String?,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
)
