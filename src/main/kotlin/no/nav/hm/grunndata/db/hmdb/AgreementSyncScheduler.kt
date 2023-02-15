package no.nav.hm.grunndata.db.hmdb

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.LeaderElection
import org.slf4j.LoggerFactory

@Singleton
@Requires(property = "schedulers.enabled", value = "true")
class AgreementSyncScheduler(private val agreementSync: AgreementSync,
                             private val leaderElection: LeaderElection) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementSyncScheduler::class.java)
    }

    @Scheduled(cron="0 30 0 * * *")
    fun syncAgreements() {
        if (leaderElection.isLeader()) {
            agreementSync.syncAgreements()
        }
    }

}