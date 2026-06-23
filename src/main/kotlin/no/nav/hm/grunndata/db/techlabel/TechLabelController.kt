package no.nav.hm.grunndata.db.techlabel

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/api/v1/techlabels")
class TechLabelController(private val techLabelService: TechLabelService) {

    @Get("/")
    fun getAllTechLabels(): Map<String, List<TechLabelDTO>> =
        techLabelService.fetchAllLabels()

}