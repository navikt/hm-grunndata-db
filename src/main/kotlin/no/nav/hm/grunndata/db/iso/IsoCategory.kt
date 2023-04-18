package no.nav.hm.grunndata.db.iso

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO
import no.nav.hm.grunndata.rapid.dto.IsoTranslations
import java.time.LocalDateTime



@MappedEntity("isocategory_v1")
data class IsoCategory(
    @field:Id
    val isoCode: String,
    val isoTitle: String,
    val isoText: String,
    val isoTextShort: String,
    val isoTextLong: String,
    @field:TypeDef(type = DataType.JSON)
    val isoTranslations: IsoTranslations = IsoTranslations(),
    val isoLevel: Int,
    val isActive: Boolean = true,
    val showTech: Boolean = true,
    val allowMulti: Boolean = true,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)



fun IsoCategory.toDTO(): IsoCategoryDTO = IsoCategoryDTO(
    isoCode = isoCode,
    isoTitle = isoTitle,
    isoText = isoText,
    isoTextShort = isoTextShort,
    isoTextLong = isoTextLong,
    isoTranslations = isoTranslations,
    isoLevel = isoLevel,
    isActive = isActive,
    showTech = showTech,
    allowMulti = allowMulti
)
