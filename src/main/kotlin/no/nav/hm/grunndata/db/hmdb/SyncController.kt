package no.nav.hm.grunndata.db.hmdb

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import org.slf4j.LoggerFactory

@Controller("/internal/sync")
class SyncController(private val productSyncScheduler: ProductSyncScheduler,
                     private val agreementSync: AgreementSync,
                     private val supplierSync: SupplierSync) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SyncController::class.java)
    }

    @Get("/suppliers")
    fun syncSuppliers() {
        LOG.info("call sync suppliers from HMDB")
        supplierSync.syncSuppliers()
    }

    @Get("/agreements")
    fun syncAgreements() {
        LOG.info("call sync agreements from HMDB")
        agreementSync.syncAgreements()
    }

    @Get("/products")
    fun syncProducts() {
        LOG.info("call sync products from HMDB")
        productSyncScheduler.syncProducts()
    }

}