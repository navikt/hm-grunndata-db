package no.nav.hm.grunndata.db.servicejob

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class ServiceJobService(private val repository: ServiceJobRepository) {

    suspend fun findServiceJobs(
        buildCriteriaSpec: PredicateSpecification<ServiceJob>? = null,
        pageable: Pageable
    ): Page<ServiceJob> {
        return repository.findAll(buildCriteriaSpec, pageable)
    }

    suspend fun findBySupplierId(supplierId: UUID): List<ServiceJob> =
        repository.findBySupplierId(supplierId)

}
