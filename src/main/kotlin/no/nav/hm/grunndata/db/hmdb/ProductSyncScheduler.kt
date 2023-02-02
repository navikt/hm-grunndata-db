package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.exceptions.DataAccessException
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.hmdb.product.HmDBProductMapper
import no.nav.hm.grunndata.db.hmdb.product.HmDbProductBatchDTO
import no.nav.hm.grunndata.db.product.*
import no.nav.hm.grunndata.db.rapid.EventNames
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.transaction.Transactional


@Singleton
open class ProductSyncScheduler(
                    private val hmdbBatchRepository: HmDbBatchRepository,
                    private val hmDBProductMapper: HmDBProductMapper,
                    private val hmDbClient: HmDbClient,
                    private val productService: ProductService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductSyncScheduler::class.java)
    }

    //@Scheduled(fixedDelay = "1m")
    fun syncProducts() {
        val syncBatchJob = hmdbBatchRepository.findByName(SYNC_PRODUCTS) ?:
        hmdbBatchRepository.save(HmDbBatch(name= SYNC_PRODUCTS,
            syncfrom = LocalDateTime.now().minusYears(12).truncatedTo(ChronoUnit.SECONDS)))
        val from = syncBatchJob.syncfrom
        val to = from.plusMonths(2)
        LOG.info("Calling product sync from ${from} to $to")
        hmDbClient.fetchProducts(from, to)?.let { hmdbProductsBatch ->
            LOG.info("Got total of ${hmdbProductsBatch.products.size} products")
            runBlocking {
                try {
                    val products = extractProductBatch(hmdbProductsBatch)
                    products.forEach {
                        LOG.info("saving to db: ${it.identifier}")
                        productService.saveAndPushTokafka(it.toDTO())
                    }
                    val last = products.last()
                    LOG.info("finished batch and update last sync time ${last.updated}")
                    hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = last.updated))
                }
                catch (e: Exception) {
                    LOG.error("Got exception while syncing products", e)
                }
            }
        } ?: run {
            if (to.isBefore(LocalDateTime.now().minusHours(24))){
                LOG.info("Empty list, skip to next batch $to")
                hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = to))
            }
        }
    }





    private suspend fun extractProductBatch(batch: HmDbProductBatchDTO): List<Product> {
       return batch.products.map { prod ->
           LOG.info("Mapping product prodid: ${prod.prodid} artid: ${prod.artid} artno: ${prod.artno} from supplier ${prod.supplier}")
           hmDBProductMapper.mapProduct(prod, batch)
        }.sortedBy { it.updated }
    }
}

fun String.HmDbIdentifier(): String = "$HMDB-$this"
