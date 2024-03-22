package no.nav.hm.grunndata.db.hmdb.supplier

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.LeaderElection
import org.slf4j.LoggerFactory

@Singleton
@Requires(property = "schedulers.enabled", value = "true")
class SupplierSyncScheduler(private val supplierSync: SupplierSync,
                            private val leaderElection: LeaderElection) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierSyncScheduler::class.java)
    }

    @Scheduled(cron = "0 15 * * * *")
    fun syncSuppliers() {
        if (leaderElection.isLeader()) {
            runBlocking {
                supplierSync.syncSuppliers()
            }
        }
    }

    @Scheduled(cron="0 15 2 * * * ")
    fun syncAllSuppliers() {
        if (leaderElection.isLeader()) {
            runBlocking {
                supplierSync.syncAllSuppliers()
            }
        }
    }


}
