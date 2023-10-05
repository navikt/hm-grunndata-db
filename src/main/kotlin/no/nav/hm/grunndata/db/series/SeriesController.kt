package no.nav.hm.grunndata.db.series

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import no.nav.hm.grunndata.rapid.dto.SeriesRapidDTO
import org.slf4j.LoggerFactory
import java.util.*

@Controller(SeriesController.API_V1_SERIES)
class SeriesController(private val seriesSeries: SeriesService) {

    companion object {
        const val API_V1_SERIES = "/api/v1/series"
        private val LOG = LoggerFactory.getLogger(SeriesController::class.java)
    }

    @Get("/{?params*}")
    suspend fun findSeries(@QueryValue params: Map<String, String>?,
                             pageable: Pageable
    ): Page<SeriesRapidDTO> = seriesSeries.findSeries(params, pageable)


    @Get("/{id}")
    fun findById(id: UUID) = seriesSeries.findById(id)

    @Get("/supplier/{supplierId}")
    suspend fun findBySupplierId(supplierId: UUID) = seriesSeries.findBySupplierId(supplierId)


}