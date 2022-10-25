package no.nav.hm.grunndata.db.hmdb

data class TekniskeDataDTO(
    val prodid: String,
    val artid: String,
    val datavalue: String?,
    val techdataunit: String?,
    val techlabeldk: String?,
)
