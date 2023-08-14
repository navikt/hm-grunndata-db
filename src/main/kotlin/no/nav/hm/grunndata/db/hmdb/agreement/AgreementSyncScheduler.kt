package no.nav.hm.grunndata.db.hmdb.agreement

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.LeaderElection
import org.slf4j.LoggerFactory

@Singleton
@Requires(property = "schedulers.enabled", value = "true")
class AgreementSyncScheduler(
    private val agreementSync: AgreementSync,
    private val leaderElection: LeaderElection
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementSyncScheduler::class.java)
    }

    @Scheduled(cron = "0 30 0 * * *")
    fun syncAgreements() {
        if (leaderElection.isLeader()) {
            runBlocking {
                agreementSync.syncAgreements()
            }
        }

    }

    @Scheduled(cron = "0 30 1 * * *")
    fun syncActiveIds() {
        if (leaderElection.isLeader()) {
            runBlocking {
                agreementSync.syncDeletedAgreementIds()
            }
        }
    }

}