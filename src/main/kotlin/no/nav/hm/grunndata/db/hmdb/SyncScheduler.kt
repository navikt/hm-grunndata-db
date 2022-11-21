package no.nav.hm.grunndata.db.hmdb

import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.agreement.*
import no.nav.hm.grunndata.db.product.*
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Singleton
class SyncScheduler(private val hmDbClient: HmDbClient,
                    private val hmdbBatchRepository: HmDbBatchRepository,
                    private val supplierRepository: SupplierRepository,
                    private val agreementRepository: AgreementRepository,
                    private val agreementPostRepository: AgreementPostRepository,
                    private val productRepository: ProductRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SyncScheduler::class.java)
    }

    @Scheduled(cron = "0 15 * * * *")
    fun syncSuppliers() {
        val syncBatchJob = hmdbBatchRepository.findByName(SYNC_SUPPLIERS) ?:
            hmdbBatchRepository.save(HmDbBatch(name= SYNC_SUPPLIERS,
                syncfrom = LocalDateTime.now().minusYears(100).truncatedTo(ChronoUnit.SECONDS)))
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

    @Scheduled(cron="0 30 * * * *")
    fun syncAgreements() {
        val syncBatchJob = hmdbBatchRepository.findByName(SYNC_AGREEMENTS) ?:
        hmdbBatchRepository.save(HmDbBatch(name= SYNC_AGREEMENTS,
            syncfrom = LocalDateTime.now().minusYears(100).truncatedTo(ChronoUnit.SECONDS)))
        val hmdbagreements = hmDbClient.fetchAgreements()
        LOG.info("Calling agreement sync from ${syncBatchJob.syncfrom}, Got total of ${hmdbagreements.size} agreements")
        val agreements = hmdbagreements.map { mapAgreement(it) }
        runBlocking {
            agreements.forEach {
                agreementRepository.findByIdentifier(it.agreement.identifier)?.let { agree ->
                    updateAgreement(it, agree)
                } ?: run {
                    saveAgreement(it)
                }
            }
            hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = agreements.last().agreement.updated))
        }
    }

    @Scheduled(cron="0 45 * * * *")
    fun syncProducts() {
        val syncBatchJob = hmdbBatchRepository.findByName(SYNC_PRODUCTS) ?:
        hmdbBatchRepository.save(HmDbBatch(name= SYNC_PRODUCTS,
            syncfrom = LocalDateTime.now().minusYears(100).truncatedTo(ChronoUnit.SECONDS)))
        val hmdbProductsBatch = hmDbClient.fetchProducts(syncBatchJob.syncfrom)
        LOG.info("Calling product sync from ${syncBatchJob.syncfrom}, Got total of ${hmdbProductsBatch.products.size} products")
        runBlocking {
            extractProductBatch(hmdbProductsBatch)
        }
    }

    private suspend fun extractProductBatch(batch: HmDbProductBatchDTO): List<Product> {
       return batch.products.map { prod ->
            Product(
                supplierId =  supplierRepository.findByIdentifier(prod.supplier!!.HmDbIdentifier())!!.id,
                title = prod.prodname,
                description = Description(),
                status = ProductStatus.ACTIVE,
                HMSArtNr = prod.stockid,
                identifier = "${prod.artid}".HmDbIdentifier(),
                supplierRef = prod.artno ?: prod.artid.toString(),
                isoCategory = prod.isocode,
                seriesId = "${prod.prodid}".HmDbIdentifier(),
                techData = mapTechData(batch.techdata[prod.artid] ?: emptyList()),
                media =  mapBlobs(batch.blobs[prod.prodid] ?: emptyList()),
                agreementInfo = prod.newsid?.let {AgreementInfo(identifier = prod.newsid.HmDbIdentifier(),
                    rank = prod.postrank!!, postId = prod.apostid!!)},
                created = prod.aindate,
                expired = prod.aoutdate,
                updated = if (prod.achange.isAfter(prod.pchange)) prod.achange else prod.pchange,
                createdBy = HMDB,
                updatedBy = HMDB
            )
        }.sortedBy { it.updated }
           .map {
                productRepository.findByIdentifier(it.identifier)?.let { inDb ->
                    productRepository.update(it.copy(id=inDb.id, created = inDb.created))
                } ?: run {
                    productRepository.save(it)
                }
            }
    }

    private fun mapTechData(techDataDTOS: List<TechDataDTO>): List<TechData> {
        TODO("Not yet implemented")
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
    identifier = "hmdb-$apostid",
    nr = apostnr,
    title = aposttitle,
    description = apostdesc
)


private fun NewsDTO.toAgreement(): Agreement = Agreement(
        identifier = "hmdb-${newsid}",
        title= newstitle,
        resume= newsresume,
        text = newstext,
        link = newslink,
        publish = newspublish,
        expire = newsexpire,
        reference = externid
)

fun String.HmDbIdentifier(): String = "$HMDB-$this"
