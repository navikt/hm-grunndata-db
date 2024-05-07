package no.nav.hm.grunndata.db.internal

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import no.nav.hm.grunndata.db.hmdb.product.ProductSyncScheduler
import no.nav.hm.grunndata.db.product.DigihotSortiment

@Controller("/internal")
class AliveController(private val productSyncScheduler: ProductSyncScheduler, private val digihotSortiment: DigihotSortiment) {
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

    // FIXME: Remove again
    @Post("/test1")
    fun test1()  = digihotSortiment.cachedBestillingsordning()

    // FIXME: Remove again
    @Post("/test2")
    fun test2()  = digihotSortiment.cachedDigitalSoknad()

    // FIXME: Remove again
    @Post("/test3")
    fun test3()  = digihotSortiment.cachedPakrevdGodkjenningskurs()

    // FIXME: Remove again
    @Post("/test4")
    fun test4()  = digihotSortiment.cachedProdukttype()

    // FIXME: Remove again
    @Post("/test5")
    fun test5()  = digihotSortiment.cachedIsoMetadata()
}