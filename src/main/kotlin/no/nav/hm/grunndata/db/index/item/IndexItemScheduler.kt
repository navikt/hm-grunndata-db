package no.nav.hm.grunndata.db.index.item

import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.register.leaderelection.LeaderOnly

@Singleton
@Requires(property = "schedulers.enabled", value = "true")
@Requires(property = "index.item.enabled", value = "true")
open class IndexItemScheduler(private val indexItemService: IndexItemService,
                         @Value("\${index.item.size}") private val size: Int ) {

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(IndexItemScheduler::class.java)
    }

    @Scheduled(fixedDelay = "10s")
    open fun indexItems() = runBlocking {
        LOG.info("Indexing items with size: $size")
        indexItemService.processPendingIndexItems(size)

    }

    @LeaderOnly
    @Scheduled(cron = "0 0 * * * *")
    open fun deleteOldIndexItems() = runBlocking {
        LOG.info("Deleting old index items")
        indexItemService.deleteOldIndexItems()
    }

}