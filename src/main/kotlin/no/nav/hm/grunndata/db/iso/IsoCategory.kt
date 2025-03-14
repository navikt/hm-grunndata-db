package no.nav.hm.grunndata.db.iso

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import java.time.LocalDateTime
import java.util.*
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO
import no.nav.hm.grunndata.rapid.dto.IsoTranslationsDTO


@MappedEntity("isocategory_v1")
data class IsoCategory(
    @field: Id
    val id: UUID = UUID.randomUUID(),
    val isoCode: String,
    val isoTitle: String,
    val isoTitleShort: String?,
    val isoText: String,
    val isoTextShort: String?,
    @field:TypeDef(type = DataType.JSON)
    val isoTranslations: IsoTranslations = IsoTranslations(),
    val isoLevel: Int,
    val isActive: Boolean = true,
    val showTech: Boolean = true,
    val allowMulti: Boolean = true,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    @field:TypeDef(type = DataType.JSON)
    val searchWords: List<String> = emptyList()
)

data class IsoTranslations(
    val titleEn: String?=null,
    val textEn: String?=null,
)

fun IsoCategory.toDTO(): IsoCategoryDTO = IsoCategoryDTO(
    isoCode = isoCode,
    isoTitle = isoTitle,
    isoTitleShort = isoTitleShort,
    isoText = isoText,
    isoTextShort = isoTextShort,
    isoTranslations = IsoTranslationsDTO(titleEn = isoTranslations.titleEn, textEn = isoTranslations.textEn),
    isoLevel = isoLevel,
    isActive = isActive,
    showTech = showTech,
    allowMulti = allowMulti,
    searchWords = searchWords,
    updated = updated,
    created = created
)


