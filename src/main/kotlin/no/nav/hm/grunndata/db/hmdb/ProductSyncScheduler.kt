package no.nav.hm.grunndata.db.hmdb

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.hm.grunndata.db.LeaderElection
import org.slf4j.LoggerFactory


@Singleton
@Requires(bean = KafkaRapid::class)
@Requires(property = "schedulers.enabled", value = "true")
open class ProductSyncScheduler(private val productSync: ProductSync,
                                private val leaderElection: LeaderElection) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductSyncScheduler::class.java)
        private var stopped = false
    }

    @Scheduled(fixedDelay = "1m")
    fun syncProducts() {
        if (leaderElection.isLeader()) {
            if (stopped) {
                LOG.warn("scheduler is stopped, maybe because of uncaught errors!")
                return
            }
            runBlocking {
                try {
                    productSync.syncProducts()
                } catch (e: Exception) {
                    LOG.error("Got uncaught exception while run product sync, stop scheduler", e)
                    stopped = true
                }
            }
        }
    }

    @Scheduled(cron = "0 30 22 * * *")
    fun syncActiveIds() {
        if (leaderElection.isLeader()) {
            if (stopped) {
                LOG.warn("scheduler is stopped, maybe because of uncaught errors!")
                return
            }
            runBlocking {
                try {
                    productSync.syncHMDBProductStates()
                } catch (e: Exception) {
                    LOG.error("Got uncaught exception while run product deleted sync, stop scheduler", e)
                    stopped = true
                }
            }
        }
    }

}
