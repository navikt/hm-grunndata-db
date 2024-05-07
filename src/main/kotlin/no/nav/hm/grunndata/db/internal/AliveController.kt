package no.nav.hm.grunndata.db.internal

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import no.nav.hm.grunndata.db.hmdb.product.ProductSyncScheduler

@Controller("/internal")
class AliveController(private val productSyncScheduler: ProductSyncScheduler) {
    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(AliveController::class.java)
    }
    @Get("/isAlive")
    fun alive(): String {
        if (productSyncScheduler.isStopped()) {
            LOG.error("ProductSyncScheduler is stopped")
            throw IllegalStateException("ProductSyncScheduler is stopped")
        }
        return "ALIVE"
    }

    @Get("/isReady")
    fun ready() = "OK"

}