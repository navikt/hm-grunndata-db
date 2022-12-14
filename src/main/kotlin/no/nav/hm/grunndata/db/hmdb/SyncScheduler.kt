package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.exceptions.DataAccessException
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.agreement.*
import no.nav.hm.grunndata.db.hmdb.agreement.AvtalePostDTO
import no.nav.hm.grunndata.db.hmdb.agreement.HmDbAgreementDTO
import no.nav.hm.grunndata.db.hmdb.agreement.NewsDTO
import no.nav.hm.grunndata.db.hmdb.product.*
import no.nav.hm.grunndata.db.product.*
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Singleton
class SyncScheduler(private val hmDbClient: HmDbClient,
                    private val hmdbBatchRepository: HmDbBatchRepository,
                    private val supplierRepository: SupplierRepository,
                    private val agreementRepository: AgreementRepository,
                    private val agreementPostRepository: AgreementPostRepository,
                    private val productRepository: ProductRepository,
                    private val hmDBProductMapper: HmDBProductMapper) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SyncScheduler::class.java)
    }

    //@Scheduled(cron = "0 15 * * * *")
    fun syncSuppliers() {
        val syncBatchJob = hmdbBatchRepository.findByName(SYNC_SUPPLIERS) ?:
            hmdbBatchRepository.save(HmDbBatch(name= SYNC_SUPPLIERS,
                syncfrom = LocalDateTime.now().minusYears(20).truncatedTo(ChronoUnit.SECONDS)))
        val suppliers = hmDbClient.fetchSuppliers(syncBatchJob.syncfrom)
        LOG.info("Calling supplier sync from ${syncBatchJob.syncfrom}, Got total of ${suppliers.size} suppliers")
        val entities = suppliers.map { it.toSupplier() }.sortedBy { it.updated }
        runBlocking {
            entities.forEach {
                val saved = supplierRepository.findByIdentifier(it.identifier)?.let { inDb->
                    supplierRepository.update(it.copy(id=inDb.id, created = inDb.created))
                } ?: run {
                    supplierRepository.save(it)
                }
                LOG.info("saved supplier ${saved.id} with identifier ${saved.identifier} and lastupdated ${saved.updated}")
            }
            hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = suppliers.last().lastupdated))
        }
    }

    //@Scheduled(cron="0 30 * * * *")
    fun syncAgreements() {
        val syncBatchJob = hmdbBatchRepository.findByName(SYNC_AGREEMENTS) ?:
        hmdbBatchRepository.save(HmDbBatch(name= SYNC_AGREEMENTS,
            syncfrom = LocalDateTime.now().minusYears(10).truncatedTo(ChronoUnit.SECONDS)))
        val hmdbagreements = hmDbClient.fetchAgreements()
        LOG.info("Calling agreement sync, got total of ${hmdbagreements.size} agreements")
        val agreements = hmdbagreements.map { mapAgreement(it) }
        runBlocking {
            agreements.forEach {
                agreementRepository.findByIdentifier(it.agreement.identifier)?.let { agree ->
                    LOG.info("updating agreement ${agree.id} with identifier ${agree.identifier}")
                    updateAgreement(it, agree)
                } ?: run {
                    LOG.info("saved new agreement ${it.agreement.id}")
                    saveAgreement(it)
                }
            }
            hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = agreements.last().agreement.updated))
        }
    }

    //@Scheduled(fixedDelay = "1m")
    fun syncProducts() {
        val syncBatchJob = hmdbBatchRepository.findByName(SYNC_PRODUCTS) ?:
        hmdbBatchRepository.save(HmDbBatch(name= SYNC_PRODUCTS,
            syncfrom = LocalDateTime.now().minusYears(10).truncatedTo(ChronoUnit.SECONDS)))
        val from = syncBatchJob.syncfrom
        val to = from.plusMonths(2)
        LOG.info("Calling product sync from ${from} to $to")
        val hmdbProductsBatch = hmDbClient.fetchProducts(from, to)
        LOG.info("Got total of ${hmdbProductsBatch.products.size} products")
        if (hmdbProductsBatch.products.isNotEmpty()) {
            runBlocking {
                val products = extractProductBatch(hmdbProductsBatch)
                products.forEach {
                    try {
                        LOG.info("finding from db: ${it.identifier}")
                        productRepository.findByIdentifier(it.identifier)?.let { inDb ->
                            productRepository.update(it.copy(id = inDb.id, created = inDb.created))
                        } ?: run {
                            productRepository.save(it)
                        }
                    }
                    catch (e: DataAccessException) {
                        LOG.error("Got exception while persisting ${it.supplierId}-${it.supplierRef} ${it.identifier}",e)
                    }
                }
                val last = products.last()
                LOG.info("finished batch and update last sync time ${last.updated}")
                hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = last.updated))
            }
        }
        else if (to.isBefore(LocalDateTime.now().minusHours(1))){
            LOG.info("Empty list, skip to next batch $to")
            hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = to))
        }

    }

    private suspend fun extractProductBatch(batch: HmDbProductBatchDTO): List<Product> {
       return batch.products.map { prod ->
           LOG.info("Mapping product prodid: ${prod.prodid} artid: ${prod.artid} artno: ${prod.artno} from supplier ${prod.supplier}")
           hmDBProductMapper.mapProduct(prod, batch)
        }.sortedBy { it.updated }
    }

    private suspend fun updateAgreement(agreementDocument: AgreementDocument, agree: Agreement) {
        agreementRepository.update(agreementDocument.agreement.copy(id = agree.id, created = agree.created))
        agreementDocument.agreementPost.forEach { post ->
            agreementPostRepository.findByIdentifier(post.identifier)?.let { db ->
                agreementPostRepository.update(post.copy(id = db.id, agreementId = agree.id, created = db.created))
            } ?: run {
                agreementPostRepository.save(post.copy(agreementId = agree.id))
            }
        }
    }

    private suspend fun saveAgreement(agreementDocument: AgreementDocument) {
        val saved = agreementRepository.save(agreementDocument.agreement)
        agreementDocument.agreementPost.forEach { post ->
            agreementPostRepository.save(post.copy(agreementId = saved.id))
        }
    }

    private fun mapAgreement(hmdbag: HmDbAgreementDTO): AgreementDocument =
        AgreementDocument(agreement = hmdbag.newsDTO.toAgreement(),
            agreementPost = hmdbag.poster.map { it.toAgreementPost() }  )
}

private fun AvtalePostDTO.toAgreementPost(): AgreementPost = AgreementPost(
    identifier = "$apostid".HmDbIdentifier(),
    nr = apostnr,
    title = aposttitle,
    description = apostdesc
)


private fun NewsDTO.toAgreement(): Agreement = Agreement(
        identifier = "$newsid".HmDbIdentifier(),
        title= newstitle,
        resume= newsresume,
        text = newstext,
        link = newslink,
        publish = newspublish,
        expire = newsexpire,
        reference = externid
)

fun String.HmDbIdentifier(): String = "$HMDB-$this"
