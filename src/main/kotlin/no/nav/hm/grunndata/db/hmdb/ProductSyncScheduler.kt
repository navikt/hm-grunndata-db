package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.exceptions.DataAccessException
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.hmdb.product.HmDBProductMapper
import no.nav.hm.grunndata.db.hmdb.product.HmDbProductBatchDTO
import no.nav.hm.grunndata.db.product.HMDB
import no.nav.hm.grunndata.db.product.Product
import no.nav.hm.grunndata.db.product.ProductRepository
import no.nav.hm.grunndata.db.product.toDTO
import no.nav.hm.grunndata.db.rapid.EventNames
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


@Singleton
class ProductSyncScheduler(private val productRepository: ProductRepository,
                    private val hmdbBatchRepository: HmDbBatchRepository,
                    private val hmDBProductMapper: HmDBProductMapper,
                    private val hmDbClient: HmDbClient,
                    private val rapidPushService: RapidPushService
) {

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
                val products = extractProductBatch(hmdbProductsBatch)
                products.forEach {
                    try {
                        LOG.info("finding from db: ${it.identifier}")
                        val saved = productRepository.findByIdentifier(it.identifier)?.let { inDb ->
                            productRepository.update(it.copy(id = inDb.id, created = inDb.created))
                        } ?: productRepository.save(it)
                        rapidPushService.pushToRapid(key = "${EventNames.hmdbproductsync}-${saved.id}",
                            eventName = EventNames.hmdbproductsync, payload = saved.toDTO())
                    }
                    catch (e: DataAccessException) {
                        LOG.error("Got exception while persisting ${it.supplierId}-${it.supplierRef} ${it.identifier}",e)
                    }
                }
                val last = products.last()
                LOG.info("finished batch and update last sync time ${last.updated}")
                hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = last.updated))
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
