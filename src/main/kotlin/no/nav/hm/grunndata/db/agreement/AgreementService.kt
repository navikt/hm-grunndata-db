package no.nav.hm.grunndata.db.agreement

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.CacheInvalidate
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.rapid.dto.AgreementStatus
import java.time.LocalDateTime
import java.util.*

@Singleton
@CacheConfig("agreements")
open class AgreementService(private val agreementRepository: AgreementRepository) {

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
}
