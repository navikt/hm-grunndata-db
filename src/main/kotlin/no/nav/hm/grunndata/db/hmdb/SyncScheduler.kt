package no.nav.hm.grunndata.db.hmdb

import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.agreement.*
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Singleton
class SyncScheduler(private val hmDbClient: HmDbClient,
                    private val hmdbBatchRepository: HmDbBatchRepository,
                    private val supplierRepository: SupplierRepository,
                    private val agreementRepository: AgreementRepository,
                    private val agreementPostRepository: AgreementPostRepository) {

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
                it.agreement.identifier
                agreementRepository.save(it.agreement)
            }
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
    desc = apostdesc
)


private fun NewsDTO.toAgreement(): Agreement = Agreement(
        identifier = "hmdb-+${externid}",
        title= newstitle,
        resume= newsresume,
        text = newstext,
        link = newslink,
        publish = newspublish,
        expire = newsexpire
)

