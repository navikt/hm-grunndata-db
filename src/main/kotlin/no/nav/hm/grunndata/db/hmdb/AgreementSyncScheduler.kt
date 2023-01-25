package no.nav.hm.grunndata.db.hmdb

import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.agreement.*
import no.nav.hm.grunndata.db.hmdb.agreement.*
import no.nav.hm.grunndata.db.product.Media
import no.nav.hm.grunndata.db.product.MediaType
import no.nav.hm.grunndata.db.rapid.EventNames
import no.nav.hm.rapids_rivers.micronaut.KafkaRapidService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Singleton
class AgreementSyncScheduler(private val agreementDocumentService: AgreementDocumentService,
                             private val hmDbClient: HmDbClient,
                             private val hmdbBatchRepository: HmDbBatchRepository,
                             private val kafkaRapidService: KafkaRapidService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementSyncScheduler::class.java)
    }

    init {
        hmdbBatchRepository.findByName(SYNC_AGREEMENTS) ?: syncAgreements()
    }

    @Scheduled(cron="0 30 1 * * *")
    fun syncAgreements() {
        val syncBatchJob = hmdbBatchRepository.findByName(SYNC_AGREEMENTS) ?:
        hmdbBatchRepository.save(HmDbBatch(name= SYNC_AGREEMENTS,
            syncfrom = LocalDateTime.now().minusYears(10).truncatedTo(ChronoUnit.SECONDS)))
        hmDbClient.fetchAgreements()?.let { hmdbagreements ->
            LOG.info("Calling agreement sync, got total of ${hmdbagreements.size} agreements")
            val agreements = hmdbagreements.map { mapAgreement(it) }
            runBlocking {
                agreements.forEach {
                    val doc = agreementDocumentService.saveAgreementDocument(it).toDTO()
                    kafkaRapidService.pushToRapid(key = "${EventNames.hmdbagreementsync}-${doc.agreement.id}",
                        eventName = EventNames.hmdbagreementsync, payload = doc)
                }
                hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = agreements.last().agreement.updated))
            }
        }
    }

    private fun mapAgreement(hmdbag: HmDbAgreementDTO): AgreementDocument {
        val agreement = hmdbag.toAgreement()
        return AgreementDocument(agreement = agreement,
            agreementPost = hmdbag.poster.map { it.toAgreementPost(agreement) })
    }

    private fun AvtalePostDTO.toAgreementPost(agreement: Agreement): AgreementPost = AgreementPost(
        agreementId = agreement.id ,
        identifier = "$apostid".HmDbIdentifier(),
        nr = apostnr,
        title = aposttitle,
        description = apostdesc
    )


    private fun HmDbAgreementDTO.toAgreement(): Agreement = Agreement(
        identifier = "${newsDTO.newsid}".HmDbIdentifier(),
        title= newsDTO.newstitle,
        resume= newsDTO.newsresume,
        text = newsDTO.newstext,
        published = newsDTO.newspublish,
        expired = newsDTO.newsexpire,
        reference = newsDTO.externid,
        attachments =  mapNewsDocHolder(newsDocHolder)
    )

    private fun mapNewsDocHolder(newsdocHolder: List<NewsDocHolder>): List<AgreementAttachment> = newsdocHolder.map {
        AgreementAttachment(title = it.newsDoc.hmidoctitle,
            description = it.newsDoc.hmidocdesc, media = mapMedia(it.newsDoc, it.newsDocAdr)) }


    private fun mapMedia(newsDoc: NewsDocDTO, newsDocAdr: List<NewsDocAdr>): List<Media> {
        val mediaList = if (!newsDoc.hmidocfilename.isNullOrBlank())
            listOf(Media(uri=newsDoc.hmidocfilename, type = getFileType(newsDoc.hmidocfilename)))
        else emptyList()
        return mediaList.plus( newsDocAdr.map {
            Media(uri = it.docadrfile, type = getFileType(it.docadrfile))
        })
    }

    private fun getFileType(filename: String) : MediaType =
        when (filename.substringAfterLast('.', "").lowercase()) {
            "pdf" -> MediaType.PDF
            "jpg", "png" -> MediaType.IMAGE
            else -> MediaType.OTHER
        }

}