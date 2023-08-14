package no.nav.hm.grunndata.db.hmdb.product

import io.micronaut.data.exceptions.DataAccessException
import jakarta.inject.Singleton
import kotlinx.coroutines.delay

import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.hmdb.HmDbBatch
import no.nav.hm.grunndata.db.hmdb.HmDbBatchRepository
import no.nav.hm.grunndata.db.hmdb.HmDbClient
import no.nav.hm.grunndata.db.hmdb.SYNC_PRODUCTS
import no.nav.hm.grunndata.db.product.Product
import no.nav.hm.grunndata.db.product.ProductService

import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.event.EventName
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


@Singleton
open class ProductSync(
    private val hmdbBatchRepository: HmDbBatchRepository,
    private val hmDBProductMapper: HmDBProductMapper,
    private val hmDbClient: HmDbClient,
    private val productService: ProductService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductSync::class.java)
        private var lastChanged: LocalDateTime = LocalDateTime.now()
        private var lastSize: Int = -1
        private var days = 10L
    }

    suspend fun syncProducts() {
        val syncBatchJob = hmdbBatchRepository.findByName(SYNC_PRODUCTS) ?: hmdbBatchRepository.save(
            HmDbBatch(
                name = SYNC_PRODUCTS,
                syncfrom = LocalDateTime.now().minusYears(12).truncatedTo(ChronoUnit.SECONDS)
            )
        )
        val from = syncBatchJob.syncfrom
        val to = from.plusDays(days)
        LOG.info("Calling product sync from ${from} to $to")
        val hmdbProductsBatch = hmDbClient.fetchProducts(from, to)
        LOG.info("Got total of ${hmdbProductsBatch!!.products.size} products")
        val products = extractProductBatch(hmdbProductsBatch)
        LOG.info("Got products sorted size ${products.size}")
        if (lastSize == products.size && lastChanged == products.last().updated) {
            LOG.info("Last Size $lastSize and lastChanged $lastChanged is the same, skipping this batch")
            hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = from.plusSeconds(1)))
            return
        }
        if (products.isNotEmpty()) {
            products.forEach {
                try {
                    LOG.info("saving to db: ${it.identifier} with hmsnr ${it.hmsArtNr}")
                    productService.saveAndPushTokafka(it, EventName.hmdbproductsyncV1)
                } catch (e: DataAccessException) {
                    LOG.error("note we are skipping the product that has DataAccessException!", e)
                }
            }
            val last = products.last()
            LOG.info("finished batch and update last sync time ${last.updated}")
            hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = last.updated))
            lastSize = products.size
            lastChanged = last.updated
            days = 10L
        } else {
            LOG.info("Empty list")
            if (to.isBefore(LocalDateTime.now())) {
                hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = to))
                days = 30L // make it run faster
            }
        }

    }

    suspend fun syncProductsById(productId: Long) {
        hmDbClient.fetchProductsById(productId)?.let { batch ->
            extractProductBatch(batch).forEach {
                try {
                    LOG.info("saving to db: ${it.identifier} with hmsnr ${it.hmsArtNr}")
                    productService.saveAndPushTokafka(it, EventName.hmdbproductsyncV1)
                } catch (e: DataAccessException) {
                    LOG.error("got exception", e)
                }
            }
        } ?: LOG.error("Could not find $productId")
    }

    suspend fun syncAllActiveProducts() {
        val sortedIds = hmDbClient.fetchProductsIdActive()?.sorted() ?: emptyList()
        var startIndex = 0
        var endIndex = 999;
        while(endIndex < sortedIds.size) {
            val start = sortedIds.elementAt(startIndex)
            val end = sortedIds.elementAt(endIndex)
            LOG.info("from index: $startIndex to endIndex $endIndex, with $start - $end")
            syncProductsByArtIdStartEnd(start, end)
            LOG.info("Delay for 60s")
            delay(60000)
            startIndex = endIndex+1
            endIndex = startIndex + 999;
        }
        if (startIndex<sortedIds.size) {
            val start = sortedIds.elementAt(startIndex)
            val end = sortedIds.last()
            LOG.info("Getting the rest of elements with $start - $end")
            syncProductsByArtIdStartEnd(start,end)
        }
    }

    suspend fun syncProductsByArtIdStartEnd(artIdStart:Long, artIdEnd:Long) {
        hmDbClient.fetchProductsByArtIdStartEnd(artIdStart, artIdEnd)?.let {
            batch -> extractProductBatch(batch).forEach {
                try {
                    LOG.info("saving to db: ${it.identifier} with hmsnr ${it.hmsArtNr}")
                    productService.saveAndPushTokafka(it, EventName.hmdbproductsyncV1)
                }
                catch (e: Exception) {
                    LOG.error("Got exception", e)
                }
            }
        } ?: LOG.error("Could not find any from $artIdStart and $artIdEnd")
    }

    suspend fun syncHMDBProductStates() {
        val activeProductIds = productService.findIdsByStatusAndCreatedBy(ProductStatus.ACTIVE, HMDB)
            .associateBy { it.identifier.substringAfter("-").toLong()}
        val hmdbIds = hmDbClient.fetchProductsIdActive()?.toSet() ?: emptySet()
        if (hmdbIds.isNotEmpty()) {
            LOG.info("Found ${hmdbIds.size} active products in HMDB")
            val toBeDeleted = activeProductIds.filterNot { hmdbIds.contains(it.key) }
            LOG.info("Found $toBeDeleted to be deleted")
            val notInDb = hmdbIds.filterNot { activeProductIds.containsKey(it)}
            LOG.info("Found $notInDb active products not in db")
            toBeDeleted.forEach {
                productService.findById(it.value.id)?.let { inDb ->
                    productService.saveAndPushTokafka(inDb.copy(status = ProductStatus.DELETED, updatedBy = "HMDB-DELETE",
                        updated = LocalDateTime.now()), EventName.hmdbproductsyncV1)
                }
            }
            notInDb.forEach { artid ->
                LOG.info("fetching $artid")
                hmDbClient.fetchProductByArticleId(artid)?.let {
                    batch -> extractProductBatch(batch).forEach {
                        try {
                            LOG.info("saving to db: ${it.identifier} with hmsnr ${it.hmsArtNr}")
                            productService.saveAndPushTokafka(it, EventName.hmdbproductsyncV1)
                        }
                        catch (e: Exception) {
                            LOG.error("Got exception", e)
                        }
                    }
                }
            }
        }
    }


    private fun extractProductBatch(batch: HmDbProductBatchDTO): List<Product> {
        return batch.products.map { prod ->
            LOG.info("Mapping product prodid: ${prod.prodid} artid: ${prod.artid} artno: ${prod.artno} from supplier ${prod.supplier}")
            hmDBProductMapper.mapProduct(prod, batch)
        }.sortedBy { it.updated }
    }

}

fun String.HmDbIdentifier(): String = "$HMDB-$this"