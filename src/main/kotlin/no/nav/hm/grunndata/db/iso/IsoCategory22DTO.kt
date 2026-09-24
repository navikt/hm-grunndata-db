package no.nav.hm.grunndata.db.iso

import no.nav.hm.grunndata.rapid.dto.IsoTranslationsDTO
import java.time.LocalDateTime

data class IsoCategory22DTO(
    val isoCode: String,
    val isoTitle: String,
    val isoText: String,
    val isoTranslations: IsoTranslationsDTO?=null,
    val isoLevel: Int,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val searchWords: List<String> = emptyList()
)