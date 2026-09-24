package no.nav.hm.grunndata.db.internal

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Put
import no.nav.hm.grunndata.db.iso.IsoMapService
import no.nav.hm.grunndata.db.product.ProductRepository
import no.nav.hm.grunndata.db.series.SeriesRepository

@Controller("/internal")
class SeriesIso22UpdateController(private val seriesRepository: SeriesRepository,
                                  private val productRepository: ProductRepository,
                                  private val isoMapService: IsoMapService) {

    @Put("/db/mapIso22")
    suspend fun mapIso22() {
        mapIsoCode16ToIsoCode22()
    }

    suspend fun mapIsoCode16ToIsoCode22() {
        val series = seriesRepository.findAll()
        series.collect { serie ->
            isoMapService.mapIso16To22(serie.isoCategory)?.let { iso22 ->
                if (iso22.verified) {
                    seriesRepository.update(serie.copy(isoCategory22 = iso22.code22))
                    productRepository.findBySeriesUUID(serie.id).forEach {
                        productRepository.update(it.copy(isoCategory22 = iso22.code22))
                    }
                }
            } ?: run {
                LOG.warn("Could not find mapping for ${serie.isoCategory}")
            }
        }
    }

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(SeriesIso22UpdateController::class.java)
    }
}