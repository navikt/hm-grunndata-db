package no.nav.hm.grunndata.db.product

import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.LeaderElection
import org.slf4j.LoggerFactory

@Singleton
@Requires(property = "schedulers.enabled", value = "true")
class ProductExpirationScheduler(
    private val productExpiration: ProductExpiration,
    private val leaderElection: LeaderElection
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductExpirationScheduler::class.java)
    }

    //@Scheduled(cron = "0 30 2 * * *") // disabled until hmdb sync is deactivated
    fun handleExpiredProducts() {
        if (leaderElection.isLeader()) {
            LOG.info("Running expiration product scheduler")
            runBlocking {
                productExpiration.expiredProducts()
            }
        }
    }
}