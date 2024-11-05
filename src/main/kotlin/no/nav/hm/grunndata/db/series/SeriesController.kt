package no.nav.hm.grunndata.db.series

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.annotation.RequestBean
import java.time.LocalDateTime
import no.nav.hm.grunndata.rapid.dto.SeriesRapidDTO
import org.slf4j.LoggerFactory
import java.util.*

@Controller(SeriesController.API_V1_SERIES)
class SeriesController(private val seriesSeries: SeriesService) {

    companion object {
        const val API_V1_SERIES = "/api/v1/series"
        private val LOG = LoggerFactory.getLogger(SeriesController::class.java)
    }

    @Get("/")
    suspend fun findSeries(
        @RequestBean seriesCriteria: SeriesCriteria, pageable: Pageable
    ): Page<SeriesRapidDTO> = seriesSeries.findSeries(buildCriteriaSpec(seriesCriteria), pageable)

    private fun buildCriteriaSpec(crit: SeriesCriteria): PredicateSpecification<Series>? =
        if (crit.isNotEmpty()) {
            where {
                crit.supplierId?.let { root[Series::supplierId] eq it }
                crit.updated?.let { root[Series::updated] greaterThanOrEqualTo it }
                crit.status?.let { root[Series::status] eq it }
            }
        } else null


    @Get("/{id}")
    suspend fun findById(id: UUID) = seriesSeries.findByIdToDTO(id)

    @Get("/supplier/{supplierId}")
    suspend fun findBySupplierId(supplierId: UUID) = seriesSeries.findBySupplierId(supplierId)

    @Get("/find/deletedSeriesThatDoesNotExist")
    suspend fun findDeletedSeriesThatDoesNotExist() = seriesSeries.findDeletedSeriesThatDoesNotExist()

}

@Introspected
data class SeriesCriteria(
    val supplierId: UUID?,
    val updated: LocalDateTime?,
    val status: String?
) {
    fun isNotEmpty(): Boolean = supplierId != null || updated != null || status != null
}
