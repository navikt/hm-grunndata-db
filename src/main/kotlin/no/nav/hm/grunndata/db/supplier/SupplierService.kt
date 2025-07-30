package no.nav.hm.grunndata.db.supplier

import io.micronaut.cache.annotation.Cacheable
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.index.item.IndexItemService
import no.nav.hm.grunndata.db.index.item.IndexSettings
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.supplier.toDoc
import no.nav.hm.grunndata.rapid.dto.SupplierDTO
import org.slf4j.LoggerFactory
import java.util.*

@Singleton
@Cacheable(cacheNames = ["suppliers"])
open class SupplierService(private val supplierRepository: SupplierRepository,
                           private val indexItemService: IndexItemService,
                           private val indexSettings: IndexSettings,
                           private val gdbRapidPushService: GdbRapidPushService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierService::class.java)

    }

    open fun findByIdentifier(identifier: String) = runBlocking {
        supplierRepository.findByIdentifier(identifier)
    }

    @Cacheable
    fun findByIdCached(id: UUID) = runBlocking {
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
    open suspend fun saveAndPushTokafka(supplierDTO: SupplierDTO, eventName: String): SupplierDTO {
        val supplier = supplierDTO.toEntity()
        val saved = supplierRepository.findById(supplier.id)?.let { inDb ->
                update(supplier.copy(id = inDb.id, created = inDb.created,
                    createdBy = inDb.createdBy))
            } ?: save(supplier)
        LOG.info("saved: ${saved.id} ")
        gdbRapidPushService.pushDTOToKafka(supplierDTO, eventName)
        indexItemService.saveIndexItem(supplierDTO.toDoc(), IndexType.SUPPLIER, indexSettings.indexConfigMap[IndexType.SUPPLIER]!!.aliasIndexName)
        return supplierDTO
    }
}
