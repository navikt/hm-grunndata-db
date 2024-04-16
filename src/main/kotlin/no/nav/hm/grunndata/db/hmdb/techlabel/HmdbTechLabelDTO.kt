package no.nav.hm.grunndata.db.hmdb.techlabel

import no.nav.hm.grunndata.db.hmdb.product.HmDbIdentifier
import no.nav.hm.grunndata.db.techlabel.TechLabel

data class HmdbTechLabelDTO(
    val id: Long,
    val isocode: String,
    val techdatatype: String,
    val techdataguide: String,
    val techdatadef: String?,
    val techdataunit: String?,
    val techlabeldk: String,
    val techdatasort: Int,
    val characteroptions: String?
)

fun HmdbTechLabelDTO.toTechLabel(): TechLabel = TechLabel (identifier = "$id".HmDbIdentifier(), label = techlabeldk,
    definition = techdatadef, guide = techdataguide, type = techdatatype, unit = techdataunit, isocode = isocode,
    sort = techdatasort, options =  characteroptions?.split(";")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
)