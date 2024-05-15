package no.nav.hm.grunndata.db.hmdb.agreement

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.register.leaderelection.LeaderOnly
import org.slf4j.LoggerFactory

@Singleton
@Requires(property = "schedulers.enabled", value = "true")
open class AgreementSyncScheduler(
    private val agreementSync: AgreementSync,
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementSyncScheduler::class.java)
    }

    @LeaderOnly
    @Scheduled(cron = "0 30 0 * * *")
    open fun syncAgreements() {
        runBlocking {
            agreementSync.syncAgreements()
        }

    }

    @LeaderOnly
    @Scheduled(cron = "0 30 1 * * *")
    open fun syncActiveIds() {
        runBlocking {
            agreementSync.syncDeletedAgreementIds()
        }
    }

}