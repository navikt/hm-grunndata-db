package no.nav.hm.grunndata.db.iso

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO

@Client("\${grunndata.register.url}")
interface RegisterClient {

    @Get("/api/v1/isocategories")
    suspend fun getAllIsoCategories(): List<IsoCategoryDTO>

}