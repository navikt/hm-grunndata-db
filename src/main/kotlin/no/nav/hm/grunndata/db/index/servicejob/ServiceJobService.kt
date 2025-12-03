package no.nav.hm.grunndata.db.servicejob

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class ServiceJobService(private val repository: ServiceJobRepository) {

    suspend fun findServiceJobs(
        buildCriteriaSpec: ((ServiceJob) -> Boolean)? = null,
        pageable: Pageable
    ): Page<ServiceJob> {
        // For now, just return all paged, ignoring criteria spec
        return repository.findAll(pageable)
    }

    suspend fun findBySupplierId(supplierId: UUID): List<ServiceJob> =
        repository.findBySupplierId(supplierId)

}
