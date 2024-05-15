package no.nav.hm.grunndata.db.hmdb.supplier

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.register.leaderelection.LeaderOnly
import org.slf4j.LoggerFactory

@Singleton
@Requires(property = "schedulers.enabled", value = "true")
open class SupplierSyncScheduler(private val supplierSync: SupplierSync) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierSyncScheduler::class.java)
    }

    @LeaderOnly
    @Scheduled(cron = "0 15 * * * *")
    open fun syncSuppliers() {

        runBlocking {
            supplierSync.syncSuppliers()
        }
    }

    @LeaderOnly
    @Scheduled(cron = "0 15 2 * * * ")
    open fun syncAllSuppliers() {
        runBlocking {
            supplierSync.syncAllSuppliers()
        }
    }


}
