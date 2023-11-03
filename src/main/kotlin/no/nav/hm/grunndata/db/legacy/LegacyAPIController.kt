package no.nav.hm.grunndata.db.legacy

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

/**
 * An API for retrieving legacy data as the old HMDB way, this can be used for apps that are not yet migrated to the new
 */
@Controller("/api/v1/legacy")
class LegacyAPIController(private val legacyService: LegacyService) {

    @Get("/produkter")
    suspend fun getAllProductsAsLegacyProdukter(): ErstattProdukterDTO = legacyService.retrieveAllProductAndMapToLegacyDTO()

    @Get("/leverandorer")
    suspend fun getAllSuppliersAsLegacyLeverandorer(): ErstattLeverandorerDTO = legacyService.retriveAllSuppliersAndMapToLegacyDTO()

}