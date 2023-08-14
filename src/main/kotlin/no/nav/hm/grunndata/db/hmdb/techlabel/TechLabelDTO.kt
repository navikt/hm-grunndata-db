package no.nav.hm.grunndata.db.hmdb.techlabel

import no.nav.hm.grunndata.db.hmdb.product.HmDbIdentifier
import no.nav.hm.grunndata.db.techlabel.TechLabel

data class TechLabelDTO(
    val id: Long,
    val isocode: String,
    val techdatatype: String,
    val techdataguide: String,
    val techdataunit: String?,
    val techlabeldk: String
)

fun TechLabelDTO.toTechLabel(): TechLabel = TechLabel (identifier = "$id".HmDbIdentifier(), label = techlabeldk,
    guide = techdataguide, type = techdatatype, unit = techdataunit, isocode = isocode )