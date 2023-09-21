package no.nav.hm.grunndata.db.series

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.CacheInvalidate
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import io.micronaut.http.annotation.QueryValue
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import no.nav.hm.grunndata.rapid.dto.RapidDTO
import no.nav.hm.grunndata.rapid.dto.SeriesRapidDTO
import no.nav.hm.grunndata.rapid.dto.SupplierDTO
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

    @CacheInvalidate(parameters = ["identifier"])
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
        val seriesDTO = saved.toDTO()
        LOG.info("saved: ${seriesDTO.id} ")
        gdbRapidPushService.pushDTOToKafka(seriesDTO, eventName)
        return seriesDTO
    }


    private fun Series.toDTO(): SeriesRapidDTO {
        TODO("Not yet implemented")
    }

}
