package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.exceptions.DataAccessException
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
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

    @Scheduled(cron = "*/2 * * * *")
   fun updateSuppliers() {
        LOG.info("Running update hmdb suppliers")
        runBlocking {
            val supplierBatch = hmDbLeverandorerBatchRepository.findFirstOrderByCreatedDesc()
            supplierBatch?.run {
                LOG.info("Found batch with ${leverandorer.size} suppliers")
                val suppliers = toSupplierList()
                suppliers.forEach { supplier ->
                    try {
                        supplierRepository.findById(supplier.id)?.let {
                            supplierRepository.update(supplier.copy(id = it.id, created = it.created))
                        } ?: supplierRepository.insertLegacy(supplier)
                    } catch (e: Exception) {
                        LOG.error("Got exception when persisting supplier ${supplier.hmdbId}", e)
                    }
                }
                LOG.info("Finished supplier batch processing, deleting")
                hmDbLeverandorerBatchRepository.deleteAll()
            }
        }
    }

    @Scheduled(cron = "*/10 * * * *")
    fun updateProducts() {
        LOG.info("Running update hmdb products")
        runBlocking {
            val productBatch = hmDbProduktBatchRepository.findFirstOrderByCreatedDesc()
            productBatch?.run {
                LOG.info("Found batch with ${produkter.size} products")
                val products = toProductList()
                products.forEach { product ->
                    try {
                        productRepository.findBySupplierIdAndSupplierRef(product.supplierId, product.supplierRef)?.let {
                            productRepository.update(product.copy(id = it.id, created = it.created))
                        } ?: productRepository.save(product)
                    } catch (e: Exception) {
                        LOG.error("Got exception while persisting product ${product.HMDBArtId}", e)
                    }
                }
                LOG.info("Finished product batch processing, deleting")
                hmDbProduktBatchRepository.deleteAll()
            }
        }
    }

}
