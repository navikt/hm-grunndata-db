package no.nav.hm.grunndata.db.index.item

import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking

@Singleton
@Requires(property = "schedulers.enabled", value = "true")
@Requires(property = "index.item.enabled", value = "true")
class IndexItemScheduler(private val indexItemService: IndexItemService,
                         @Value("\${index.item.size}") private val size: Int ) {

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(IndexItemScheduler::class.java)
    }

    @Scheduled(fixedDelay = "10s")
    fun indexItems() = runBlocking {
        LOG.info("Indexing items with size: $size")
        indexItemService.processPendingIndexItems(size)

    }
}