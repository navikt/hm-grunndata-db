package no.nav.hm.grunndata.db.iso

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/api/v22/isomap")
class Iso22MapAPIController(private val isoMapService: IsoMapService) {

    @Get("/all-maps")
    fun getAllIsoMaps(): Map<String, IsoMapDTO> {
        LOG.info("Retrieving all iso-maps")
        return isoMapService.retrieveMappingsFromRegister()
    }

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(Iso22MapAPIController::class.java)
    }

}