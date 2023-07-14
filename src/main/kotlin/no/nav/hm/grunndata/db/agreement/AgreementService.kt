package no.nav.hm.grunndata.db.agreement

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.CacheInvalidate
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.product.Product
import no.nav.hm.grunndata.db.product.ProductService
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.rapid.dto.AgreementDTO
import no.nav.hm.grunndata.rapid.dto.AgreementRegistrationRapidDTO
import no.nav.hm.grunndata.rapid.dto.AgreementStatus
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*
import javax.transaction.Transactional

@Singleton
@CacheConfig("agreements")
open class AgreementService(private val agreementRepository: AgreementRepository,
                            private val gdbRapidPushService: GdbRapidPushService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementService::class.java)
    }
    @Cacheable
    open fun findByIdentifier(identifier: String): Agreement? = runBlocking {
        agreementRepository.findByIdentifier(identifier)
    }

    @CacheInvalidate(parameters = ["identifier"])
    open fun save(agreement: Agreement, identifier: String = agreement.identifier) = runBlocking {
        agreementRepository.save(agreement)
    }

    @CacheInvalidate(parameters = ["identifier"])
    open fun update(agreement: Agreement, identifier: String = agreement.identifier) = runBlocking {
        agreementRepository.update(agreement)
    }

    suspend fun findByStatusAndExpiredBefore(status:AgreementStatus, expired: LocalDateTime? = LocalDateTime.now())
        = agreementRepository.findByStatusAndExpiredBefore(status, expired)

    /**
     * This function is not cached.
     */
    suspend fun findById(id: UUID) = agreementRepository.findById(id)

    suspend fun findAll(spec: PredicateSpecification<Agreement>?, pageable: Pageable) = agreementRepository.findAll(spec, pageable)

    @Transactional
    open suspend fun saveAndPushTokafka(agreement: Agreement, eventName: String): AgreementDTO {
        val saved =
            (if (agreement.createdBy == HMDB) findByIdentifier(agreement.identifier)
        else findById(agreement.id))?.let { inDb ->
            update(agreement.copy(id = inDb.id, created = inDb.created,
                createdBy = inDb.createdBy))
        } ?: save(agreement)
        val agreementDTO = saved.toDTO()
        LOG.info("saved: ${agreementDTO.id} ${agreementDTO.reference}")
        gdbRapidPushService.pushDTOToKafka(agreementDTO, eventName)
        return agreementDTO
    }
}
