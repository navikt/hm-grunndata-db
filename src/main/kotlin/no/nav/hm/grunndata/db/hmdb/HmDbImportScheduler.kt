package no.nav.hm.grunndata.db.hmdb

import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.product.ProductRepository
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import org.slf4j.LoggerFactory

@Singleton
class HmDbImportScheduler(private val hmDbLeverandorerBatchRepository: HmDbLeverandorerBatchRepository,
                          private val supplierRepository: SupplierRepository,
                          private val hmDbProduktBatchRepository: HmDbProduktBatchRepository,
                          private val productRepository: ProductRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(HmDbImportScheduler::class.java)
    }

    @Scheduled(cron = "0 0 8 * * *")
    suspend fun updateSuppliers() {
        LOG.info("Running update hmdb suppliers")
        val supplierBatch = hmDbLeverandorerBatchRepository.findFirstOrderByCreatedDesc()
        val suppliers = supplierBatch.toSupplierList()
        suppliers.forEach { supplier ->
            supplierRepository.findById(supplier.id)?.let {
                supplierRepository.update(supplier.copy(id = it.id, created = it.created)) }
                ?: supplierRepository.insertLegacy(supplier)
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    suspend fun updateProducts() {
        LOG.info("Running update hmdb products")
        val productBatch = hmDbProduktBatchRepository.findFirstOrderByCreatedDesc()
        val products = productBatch.toProductList()
        products.forEach {product ->
            productRepository.findBySupplierIdAndSupplierRef(product.supplierId, product.supplierRef) ?.let {
                productRepository.update(product.copy(id = it.id, created = it.created))
            } ?: productRepository.save(product)
        }
    }

}
