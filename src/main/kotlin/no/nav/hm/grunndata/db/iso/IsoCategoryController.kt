package no.nav.hm.grunndata.db.iso

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

@Controller("/api/v1/isocategories")
class IsoCategoryController(private val isoCategoryRepository: IsoCategoryRepository) {


    @Get("/")
    suspend fun retriveAllCategories(): List<IsoCategoryDTO> =
        isoCategoryRepository.findAll().map {
            it.toDTO()
        }.toList()

}
