package no.nav.hm.grunndata.db.servicejob

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.item.IndexItemService
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.servicejob.toDoc
import java.util.UUID

@Singleton
class ServiceJobService(private val repository: ServiceJobRepository, private val indexItemService: IndexItemService) {

    suspend fun findServiceJobs(
        buildCriteriaSpec: PredicateSpecification<ServiceJob>? = null,
        pageable: Pageable
    ): Page<ServiceJob> {
        return repository.findAll(buildCriteriaSpec, pageable)
    }

    suspend fun findBySupplierId(supplierId: UUID): List<ServiceJob> =
        repository.findBySupplierId(supplierId)

    suspend fun saveAndIndex(serviceJob: ServiceJob): ServiceJob {
        val saved = repository.findById(serviceJob.id)?.let {
            repository.update(it.copy(
                title = serviceJob.title,
                supplierRef = serviceJob.supplierRef,
                hmsArtNr = serviceJob.hmsArtNr,
                isoCategory = serviceJob.isoCategory,
                published = serviceJob.published,
                expired = serviceJob.expired,
                status = serviceJob.status,
                attributes = serviceJob.attributes,
                agreements = serviceJob.agreements,
                updated = serviceJob.updated,
                updatedBy = serviceJob.updatedBy
            ))
        } ?: repository.save(serviceJob)
        indexItemService.saveIndexItem(saved.toDoc(), IndexType.SERVICEJOB)
        return saved
    }

}