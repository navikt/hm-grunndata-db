package no.nav.hm.grunndata.db.hmdb.news

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.register.leaderelection.LeaderOnly

@Singleton
@Requires(property = "schedulers.enabled", value = "true")
open class NewsSyncScheduler(
    private val newsSync: NewsSync,
) {


//    @LeaderOnly
//    @Scheduled(cron = "0 45 0 * * *")
//    open fun syncNews() {
//
//        runBlocking {
//            newsSync.syncNews()
//        }
//    }
}