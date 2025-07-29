package no.nav.hm.grunndata.db.index.item

import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking

@Singleton
class IndexItemScheduler(private val indexItemService: IndexItemService) {

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(IndexItemScheduler::class.java)
    }
    @Scheduled(fixedDelay = "10s")
    fun indexItems() = runBlocking {
        LOG.info("Starting to process pending index items")
        indexItemService.processPendingIndexItems(size = 1000)

    }
}