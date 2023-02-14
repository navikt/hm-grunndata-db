package no.nav.hm.grunndata.db.hmdb

import io.micronaut.context.annotation.Requires
import io.micronaut.data.exceptions.DataAccessException
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.hmdb.product.HmDBProductMapper
import no.nav.hm.grunndata.db.hmdb.product.HmDbProductBatchDTO
import no.nav.hm.grunndata.db.product.ProductService
import no.nav.hm.grunndata.dto.ProductDTO
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


@Singleton
@Requires(bean = KafkaRapid::class)
@Requires(property = "schedulers.enabled", value = "true")
open class ProductSyncScheduler(
    private val hmdbBatchRepository: HmDbBatchRepository,
    private val hmDBProductMapper: HmDBProductMapper,
    private val hmDbClient: HmDbClient,
    private val productService: ProductService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductSyncScheduler::class.java)
        private var stopped = false
    }

    @Scheduled(fixedDelay = "60s")
    fun syncProducts() {
        if (stopped) {
            LOG.warn("scheduler is stopped, maybe because of uncaught errors!")
            return
        }
        try {
            runBlocking {
                val syncBatchJob = hmdbBatchRepository.findByName(SYNC_PRODUCTS) ?: hmdbBatchRepository.save(
                    HmDbBatch(
                        name = SYNC_PRODUCTS,
                        syncfrom = LocalDateTime.now().minusYears(12).truncatedTo(ChronoUnit.SECONDS)
                    )
                )
                val from = syncBatchJob.syncfrom
                val to = from.plusMonths(2)
                LOG.info("Calling product sync from ${from} to $to")
                hmDbClient.fetchProducts(from, to)?.let { hmdbProductsBatch ->
                    LOG.info("Got total of ${hmdbProductsBatch.products.size} products")
                    val products = extractProductBatch(hmdbProductsBatch)
                    products.forEach {
                        try {
                            LOG.info("saving to db: ${it.identifier} with hmsnr ${it.hmsArtNr}")
                            productService.saveAndPushTokafka(it)
                        } catch (e: DataAccessException) {
                            LOG.error("got exception", e)
                        }
                    }
                    val last = products.last()
                    LOG.info("finished batch and update last sync time ${last.updated}")
                    hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = last.updated))

                } ?: run {
                    if (to.isBefore(LocalDateTime.now().minusHours(24))) {
                        LOG.info("Empty list, skip to next batch $to")
                        hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = to))
                    }
                }
            }
        } catch (e: Exception) {
            LOG.error("Got uncaught exception while run product sync, stop scheduler", e)
            stopped = true
        }
    }


    private suspend fun extractProductBatch(batch: HmDbProductBatchDTO): List<ProductDTO> {
        return batch.products.map { prod ->
            LOG.info("Mapping product prodid: ${prod.prodid} artid: ${prod.artid} artno: ${prod.artno} from supplier ${prod.supplier}")
            hmDBProductMapper.mapProduct(prod, batch)
        }.sortedBy { it.updated }
    }
}

fun String.HmDbIdentifier(): String = "$HMDB-$this"
