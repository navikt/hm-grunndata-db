package no.nav.hm.grunndata.db.supplier

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.CacheInvalidate
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.*

@Singleton
@CacheConfig("suppliers")
open class SupplierService(private val supplierRepository: SupplierRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierService::class.java)

    }
    @Cacheable(parameters = ["identifier"])
    open fun findByIdentifier(identifier: String) = runBlocking {
        supplierRepository.findByIdentifier(identifier)
    }

    @Cacheable(parameters = ["id"])
    open fun findById(id: UUID) = runBlocking {
        supplierRepository.findById(id)
    }

    @CacheInvalidate(parameters = ["id"])
    open fun save(supplier: Supplier, id: UUID = supplier.id) = runBlocking {
        supplierRepository.save(supplier)
    }

    @CacheInvalidate(parameters = ["id"])
    open fun update(supplier:Supplier, id: UUID = supplier.id) = runBlocking {
        supplierRepository.update(supplier)
    }

    suspend fun findAll(buildCriteriaSpec: PredicateSpecification<Supplier>?, pageable: Pageable) =
        supplierRepository.findAll(buildCriteriaSpec,pageable)

}
