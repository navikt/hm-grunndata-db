package no.nav.hm.grunndata.db.legacy

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
)
