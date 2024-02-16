package no.nav.hm.grunndata.db.hmdb.news

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.LeaderElection

@Singleton
@Requires(property = "schedulers.enabled", value = "true")
class NewsSyncScheduler(
    private val newsSync: NewsSync,
    private val leaderElection: LeaderElection
) {


    @Scheduled(cron = "0 45 0 * * *")
    fun syncNews() {
        if (leaderElection.isLeader()) {
            runBlocking {
                newsSync.syncNews()
            }
        }
    }
}