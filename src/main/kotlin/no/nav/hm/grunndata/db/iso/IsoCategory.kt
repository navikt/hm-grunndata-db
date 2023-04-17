package no.nav.hm.grunndata.db.iso

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

@MappedEntity("isocategory_v1")
data class IsoCategory(
    @field:Id
    val isoCode: String,
    val isoTitle: String,
    val isoText: String,
    val isoTextShort: String,
    val isoTextLong: String,
    val isoTitleEn: String,
    val isoTextShortEn: String,
    val isoTextLongEn: String,
    val isoLevel: Int,
    val isActive: Boolean,
    val showTech: Boolean,
    val allowMulti: Boolean,

)
