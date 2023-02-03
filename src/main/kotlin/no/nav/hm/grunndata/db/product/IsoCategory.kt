package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import java.net.URL

@Singleton
class IsoCategory(
    @Value("\${isocategory.url}")
    private val url: String,
    private val objectMapper: ObjectMapper) {

    private val catMap: Map<String, IsoCategoryDTO> = objectMapper.readValue(URL(url),
        object : TypeReference<List<IsoCategoryDTO>>(){}).associateBy { it.isokode }

    fun tittel(isokode: String): String? = catMap[isokode]?.isotittel
    fun kortnavn(isokode: String): String? = catMap[isokode]?.kortnavn
    fun isoCategory(isokode: String): IsoCategoryDTO? = catMap[isokode]
}

data class IsoCategoryDTO (
    val isokode: String,
    val isotittel: String,
    val kortnavn: String )


