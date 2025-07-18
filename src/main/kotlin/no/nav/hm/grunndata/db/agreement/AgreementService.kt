package no.nav.hm.grunndata.db.agreement

import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.rapid.dto.AgreementDTO
import no.nav.hm.grunndata.rapid.dto.AgreementStatus
import org.slf4j.LoggerFactory

@Singleton
open class AgreementService(private val agreementRepository: AgreementRepository,
                            private val gdbRapidPushService: GdbRapidPushService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementService::class.java)
    }

    open fun findByIdentifier(identifier: String): Agreement? = runBlocking {
        agreementRepository.findByIdentifier(identifier)
    }

    open fun save(agreement: Agreement, identifier: String = agreement.identifier) = runBlocking {
        agreementRepository.save(agreement)
    }

    open fun update(agreement: Agreement, identifier: String = agreement.identifier) = runBlocking {
        agreementRepository.update(agreement)
    }


    /**
     * This function is not cached.
     */
    suspend fun findById(id: UUID) = agreementRepository.findById(id)

    suspend fun findAll(criteria: AgreementCriteria, pageable: Pageable) = agreementRepository.findAll(buildCriteriaSpec(criteria), pageable)

    @Transactional
    open suspend fun saveAndPushTokafka(agreement: Agreement, eventName: String): AgreementDTO {
        val saved = findById(agreement.id)?.let { inDb ->
            update(agreement.copy(id = inDb.id, created = inDb.created,
                createdBy = inDb.createdBy))
        } ?: save(agreement)
        val agreementDTO = saved.toDTO()
        LOG.info("saved: ${agreementDTO.id} ${agreementDTO.reference}")
        gdbRapidPushService.pushDTOToKafka(agreementDTO, eventName)
        return agreementDTO
    }


    private fun buildCriteriaSpec(crit: AgreementCriteria): PredicateSpecification<Agreement>? =
        if (crit.isNotEmpty()) {
            where {
                crit.reference?.let { root[Agreement::reference] eq it }
                crit.updatedAfter?.let { root[Agreement::updated] greaterThanOrEqualTo it }
                crit.status?.let { root[Agreement::status] eq it }
                crit.expiredAfter?.let { root[Agreement::expired] greaterThanOrEqualTo it }
            }
        } else null

}
