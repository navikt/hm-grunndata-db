package no.nav.hm.grunndata.db.iso

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO
import org.slf4j.LoggerFactory


@Controller("/api/v1/isocategories")
class IsoCategoryController(private val isoCategoryService: IsoCategoryService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(IsoCategoryController::class.java)
    }

    @Get("/")
    fun retrieveAllCategories(): List<IsoCategoryDTO> {
        LOG.info("Retrieving all ISO-categories")
        return isoCategoryService.retrieveAllCategories()
    }

    @Get("/{isoCode}")
    fun lookupIsoCode(isoCode: String): IsoCategoryDTO? = isoCategoryService.lookUpCode(isoCode)

    @Get("/{isoCode}/branch")
    fun getHigherLevelsInBranch(isoCode: String): List<IsoCategoryDTO> =
        isoCategoryService.getHigherLevelsInBranch(isoCode)

}
