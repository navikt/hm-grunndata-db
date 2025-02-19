package no.nav.hm.grunndata.db.iso

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client

@Client("\${grunndata.register.url}")
interface RegisterClient {

    @Get("/api/v1/isocategories")
    suspend fun getAllIsoCategories(): List<IsoCategoryDTO>

}