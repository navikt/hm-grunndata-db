package no.nav.hm.grunndata.db.iso

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import org.slf4j.LoggerFactory


@Controller("/api/v22/isocategories")
class IsoCategory22ApiController(private val isoCategoryService: IsoCategory22Service) {

    companion object {
        private val LOG = LoggerFactory.getLogger(IsoCategory22ApiController::class.java)
    }

    @Get("/")
    fun retrieveAllCategories(): List<IsoCategory22DTO> {
        LOG.info("Retrieving all ISO-categories")
        return isoCategoryService.retrieveAllCategories()
    }

    @Get("/{isoCode}")
    fun lookupIsoCode(isoCode: String): IsoCategory22DTO? = isoCategoryService.lookUpCode(isoCode)

    @Get("/{isoCode}/branch")
    fun getHigherLevelsInBranch(isoCode: String): List<IsoCategory22DTO> =
        isoCategoryService.getHigherLevelsInBranch(isoCode)

}
