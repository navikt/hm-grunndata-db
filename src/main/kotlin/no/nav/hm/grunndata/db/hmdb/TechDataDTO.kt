package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.annotation.MappedEntity

@MappedEntity("")
data class TechDataDTO (
    val artid: String,
    val datavalue: String?,
    val techdataunit: String?,
    val techlabeldk: String?
)

