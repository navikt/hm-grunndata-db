package no.nav.hm.grunndata.db.iso

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO


@Controller("/api/v1/isocategories")
class IsoCategoryController(private val isoCategoryService: IsoCategoryService) {


    @Get("/")
    fun retrieveAllCategories(): List<IsoCategoryDTO> = isoCategoryService.retrieveAllCategories()

    @Get("/{isoCode}")
    fun lookupIsoCode(isoCode: String): IsoCategoryDTO? = isoCategoryService.lookUpCode(isoCode)

}
