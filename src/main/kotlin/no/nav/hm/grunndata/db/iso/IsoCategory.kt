package no.nav.hm.grunndata.db.iso

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import java.time.LocalDateTime
import java.util.*


@MappedEntity("isocategory_v1")
data class IsoCategory(
    @field: Id
    val id: UUID = UUID.randomUUID(),
    val isoCode: String,
    val isoTitle: String,
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
    id = id,
    isoCode = isoCode,
    isoTitle = isoTitle,
    isoText = isoText,
    isoTextShort = isoTextShort,
    isoTranslations = IsoTranslationsDTO(titleEn = isoTranslations.titleEn, textEn = isoTranslations.textEn),
    isoLevel = isoLevel,
    isActive = isActive,
    showTech = showTech,
    allowMulti = allowMulti,
    searchWords = searchWords
)

data class IsoCategoryDTO(
    val id: UUID,
    val isoCode: String,
    val isoTitle: String,
    val isoText: String,
    val isoTextShort: String?=null,
    val isoTranslations: IsoTranslationsDTO?=null,
    val isoLevel: Int,
    val isActive: Boolean = true,
    val showTech: Boolean = true,
    val allowMulti: Boolean = true,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val searchWords: List<String> = emptyList()
)

data class IsoTranslationsDTO(
    val titleEn: String?=null,
    val textEn: String?=null,
)