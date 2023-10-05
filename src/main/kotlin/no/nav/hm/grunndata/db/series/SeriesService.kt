package no.nav.hm.grunndata.db.series

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.CacheInvalidate
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.rapid.dto.SeriesRapidDTO
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

@Singleton
@CacheConfig("series")
open class SeriesService(private val seriesRepository: SeriesRepository,
                         private val gdbRapidPushService: GdbRapidPushService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SeriesService::class.java)

    }
    @Cacheable(parameters = ["identifier"])
    open fun findByIdentifier(identifier: String) = runBlocking {
        seriesRepository.findByIdentifier(identifier)
    }

    @Cacheable(parameters = ["id"])
    open fun findById(id: UUID) = runBlocking {
        seriesRepository.findById(id)
    }
    suspend fun findBySupplierId(supplierId: UUID) = seriesRepository.findBySupplierId(supplierId)

    open fun save(series: Series, identifier: String = series.identifier) = runBlocking {
        seriesRepository.save(series)
    }

    @CacheInvalidate(parameters = ["identifier"])
    open fun update(series:Series, identifier: String = series.identifier) = runBlocking {
        seriesRepository.update(series)
    }


    @Transactional
    open suspend fun saveAndPushTokafka(series: Series, eventName: String): SeriesRapidDTO {
        val saved =
            (if (series.createdBy == HMDB) findByIdentifier(series.identifier)
            else findById(series.id))?.let { inDb ->
                update(series.copy(id = inDb.id, created = inDb.created,
                    createdBy = inDb.createdBy))
            } ?: save(series)
        val seriesDTO = saved.toRapidDTO()
        LOG.info("saved: ${seriesDTO.id} ")
        gdbRapidPushService.pushDTOToKafka(seriesDTO, eventName)
        return seriesDTO
    }

    @Transactional
    open suspend fun findSeries(params: Map<String, String>?, pageable: Pageable): Page<SeriesRapidDTO> =
        seriesRepository.findAll(buildCriteriaSpec(params), pageable).map {it.toRapidDTO()}


    private fun buildCriteriaSpec(params: Map<String, String>?): PredicateSpecification<Series>?
            = params?.let {
        where {
            if (params.contains("supplierId"))  root[Series::supplierId] eq UUID.fromString(params["supplierId"]!!)
            if (params.contains("updated")) root[Series::updated] greaterThanOrEqualTo LocalDateTime.parse(params["updated"])
            if (params.contains("status")) root[Series::status] eq params["status"]
        }
    }

}
