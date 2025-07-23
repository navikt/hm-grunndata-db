package no.nav.hm.grunndata.db.agreement

import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.util.UUID
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.index.agreement.toDoc
import no.nav.hm.grunndata.db.index.item.IndexItemService
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.rapid.dto.AgreementDTO
import org.slf4j.LoggerFactory

@Singleton
open class AgreementService(private val agreementRepository: AgreementRepository,
                            private val gdbRapidPushService: GdbRapidPushService,
                            private val indexItemService: IndexItemService,
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
    open suspend fun saveAndPushTokafka(agreementDTO: AgreementDTO, eventName: String): AgreementDTO {
        val agreement = agreementDTO.toEntity()
        val saved = findById(agreement.id)?.let { inDb ->
            update(agreement.copy(id = inDb.id, created = inDb.created,
                createdBy = inDb.createdBy))
        } ?: save(agreement)
        LOG.info("saved: ${agreementDTO.id} ${agreementDTO.reference}")
        indexItemService.saveIndexItem(agreementDTO.toDoc(), IndexType.AGREEMENT)
        gdbRapidPushService.pushDTOToKafka(agreementDTO, eventName)
        LOG.info("indexing agreement id: ${agreementDTO.id} reference: ${agreementDTO.reference}")
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
