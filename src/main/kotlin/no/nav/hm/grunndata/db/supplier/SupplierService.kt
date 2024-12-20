package no.nav.hm.grunndata.db.supplier

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
import no.nav.hm.grunndata.rapid.dto.SupplierDTO
import no.nav.hm.grunndata.rapid.dto.SupplierStatus
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

@Singleton
open class SupplierService(private val supplierRepository: SupplierRepository,
                           private val gdbRapidPushService: GdbRapidPushService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierService::class.java)

    }

    open fun findByIdentifier(identifier: String) = runBlocking {
        supplierRepository.findByIdentifier(identifier)
    }


    open fun findById(id: UUID) = runBlocking {
        supplierRepository.findById(id)
    }

    suspend fun findByIdDTO(supplierId: UUID) = supplierRepository.findById(supplierId)?.toDTO()


    open fun save(supplier: Supplier, id: UUID = supplier.id) = runBlocking {
        supplierRepository.save(supplier)
    }


    open fun update(supplier:Supplier, id: UUID = supplier.id) = runBlocking {
        supplierRepository.update(supplier)
    }

    suspend fun findSuppliers(spec: PredicateSpecification<Supplier>?, pageable: Pageable
    ): Page<SupplierDTO> = supplierRepository.findAll(spec, pageable).map { it.toDTO() }



    @Transactional
    open suspend fun saveAndPushTokafka(supplier: Supplier, eventName: String): SupplierDTO {
        val saved = findById(supplier.id)?.let { inDb ->
                update(supplier.copy(id = inDb.id, created = inDb.created,
                    createdBy = inDb.createdBy))
            } ?: save(supplier)
        val supplierDTO = saved.toDTO()
        LOG.info("saved: ${supplierDTO.id} ")
        gdbRapidPushService.pushDTOToKafka(supplierDTO, eventName)
        return supplierDTO
    }
}
