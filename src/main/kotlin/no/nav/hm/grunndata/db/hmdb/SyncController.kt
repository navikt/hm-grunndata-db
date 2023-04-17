package no.nav.hm.grunndata.db.hmdb

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Put
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime

@Controller("/internal/sync")
class SyncController(private val productSync: ProductSync,
                     private val hmDbBatchRepository: HmDbBatchRepository,
                     private val agreementSync: AgreementSync,
                     private val supplierSync: SupplierSync) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SyncController::class.java)
    }

    @Get("/suppliers")
    suspend fun syncSuppliers() {
        LOG.info("call sync suppliers from HMDB")
        supplierSync.syncSuppliers()
    }

    @Get("/agreements")
    suspend fun syncAgreements() {
        LOG.info("call sync agreements from HMDB")
        agreementSync.syncAgreements()
    }

    @Get("/products")
    suspend fun syncProducts() {
        LOG.info("call sync products from HMDB")
        productSync.syncProducts()
    }

    @Get("/products/{productId}")
    suspend fun syncProductsById(productId:Long) {
        LOG.info("call sync products from HDMB for $productId")
        productSync.syncProductsById(productId)
    }

    @Put("/products/syncFrom/{syncFrom}")
    suspend fun setProductsSyncFrom(syncFrom: LocalDateTime) {
        LOG.info("Reset syncfrom for products to $syncFrom")
        hmDbBatchRepository.findByName(SYNC_PRODUCTS)?.let {
            hmDbBatchRepository.update(it.copy(syncfrom=syncFrom))
        }
    }
}
