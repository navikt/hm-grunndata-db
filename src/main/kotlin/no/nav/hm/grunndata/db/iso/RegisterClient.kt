package no.nav.hm.grunndata.db.iso

import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO
import java.time.LocalDateTime
import java.util.UUID

@Client("\${grunndata.register.url}")
interface RegisterClient {

    @Get("/api/v1/isocategories")
    suspend fun getAllIsoCategories(): List<IsoCategoryDTO>

    @Get("/api/v22/isocategories")
    suspend fun getAllIsoCategoriesV22(): List<IsoCategory22DTO>

    @Get("/api/v22/isomap")
    suspend fun getIsoMapV22(): List<IsoMapDTO>
}


@Introspected
data class IsoMapDTO(
    val id: UUID,
    val code16: String,
    val mapEnum: List<String> = emptyList(),
    val code22: String,
    val created: LocalDateTime = LocalDateTime.now(),
    val verified: Boolean = false,
    val level22: Int
)
