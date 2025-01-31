package no.nav.hm.grunndata.db.series

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.rapid.dto.SeriesRapidDTO
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

@Singleton
open class SeriesService(private val seriesRepository: SeriesRepository,
                         private val gdbRapidPushService: GdbRapidPushService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SeriesService::class.java)

    }

    open fun findByIdentifier(identifier: String) = runBlocking {
        seriesRepository.findByIdentifier(identifier)
    }

    open fun findById(id: UUID) = runBlocking {
        seriesRepository.findById(id)
    }

    suspend fun findByIdToDTO(id: UUID) = seriesRepository.findById(id)?.toRapidDTO()

    suspend fun findBySupplierId(supplierId: UUID) = seriesRepository.findBySupplierId(supplierId).map { it.toRapidDTO() }

    open fun save(series: Series, identifier: String = series.identifier) = runBlocking {
        seriesRepository.save(series)
    }

    open fun update(series:Series, identifier: String = series.identifier) = runBlocking {
        seriesRepository.update(series)
    }


    @Transactional
    open suspend fun saveAndPushTokafka(series: Series, eventName: String): SeriesRapidDTO {
        val saved = findById(series.id)?.let { inDb ->
                update(series.copy(id = inDb.id, created = inDb.created, identifier = inDb.identifier,
                    createdBy = inDb.createdBy))
            } ?: save(series)
        val seriesDTO = saved.toRapidDTO()
        LOG.info("saved: ${seriesDTO.id} ")
        gdbRapidPushService.pushDTOToKafka(seriesDTO, eventName)
        return seriesDTO
    }

    @Transactional
    open suspend fun findSeries(spec: PredicateSpecification<Series>?, pageable: Pageable): Page<SeriesRapidDTO> =
        seriesRepository.findAll(spec, pageable).map {it.toRapidDTO()}


    suspend fun findDeletedSeriesThatDoesNotExist() = seriesRepository.findDeletedSeriesThatDoesNotExist()
}
