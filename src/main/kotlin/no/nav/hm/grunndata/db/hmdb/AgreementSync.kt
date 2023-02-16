package no.nav.hm.grunndata.db.hmdb

import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.agreement.Agreement
import no.nav.hm.grunndata.db.agreement.AgreementRepository
import no.nav.hm.grunndata.db.agreement.toDTO
import no.nav.hm.grunndata.db.hmdb.agreement.*
import no.nav.hm.grunndata.rapid.dto.AgreementAttachment
import no.nav.hm.grunndata.rapid.dto.AgreementPost
import no.nav.hm.grunndata.rapid.dto.Media
import no.nav.hm.grunndata.rapid.dto.MediaType
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Singleton
@Requires(bean = KafkaRapid::class)
class AgreementSync(
    private val agreementRepository: AgreementRepository,
    private val hmDbClient: HmDbClient,
    private val hmdbBatchRepository: HmDbBatchRepository,
    private val gdbRapidPushService: GdbRapidPushService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementSync::class.java)
    }

    suspend fun syncAgreements() {
        val syncBatchJob = hmdbBatchRepository.findByName(SYNC_AGREEMENTS) ?: hmdbBatchRepository.save(
            HmDbBatch(
                name = SYNC_AGREEMENTS,
                syncfrom = LocalDateTime.now().minusYears(10).truncatedTo(ChronoUnit.SECONDS)
            )
        )
        hmDbClient.fetchAgreements()?.let { hmdbagreements ->
            LOG.info("Calling agreement sync, got total of ${hmdbagreements.size} agreements")
            val agreements = hmdbagreements.map { it.toAgreement() }
            agreements.forEach { agreement ->
                val dto = agreementRepository.findByIdentifier(agreement.identifier)?.let {
                    it.toDTO()
                } ?: agreementRepository.save(agreement).toDTO()
                gdbRapidPushService.pushDTOToKafka(dto, EventName.hmdbagreementsync)
            }
            hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = agreements.last().updated))
        }

    }

    private fun AvtalePostDTO.toAgreementPost(): AgreementPost = AgreementPost(
        identifier = "$apostid".HmDbIdentifier(),
        nr = apostnr,
        title = aposttitle,
        description = apostdesc
    )


    private fun HmDbAgreementDTO.toAgreement(): Agreement = Agreement(
        identifier = "${newsDTO.newsid}".HmDbIdentifier(),
        title = newsDTO.newstitle,
        resume = newsDTO.newsresume,
        text = newsDTO.newstext,
        published = newsDTO.newspublish,
        expired = newsDTO.newsexpire,
        reference = newsDTO.externid,
        attachments = mapNewsDocHolder(newsDocHolder),
        posts = poster.map { it.toAgreementPost() }
    )

    private fun mapNewsDocHolder(newsdocHolder: List<NewsDocHolder>): List<AgreementAttachment> = newsdocHolder.map {
        AgreementAttachment(
            title = it.newsDoc.hmidoctitle,
            description = it.newsDoc.hmidocdesc, media = mapMedia(it.newsDoc, it.newsDocAdr)
        )
    }


    private fun mapMedia(newsDoc: NewsDocDTO, newsDocAdr: List<NewsDocAdr>): List<Media> {
        val mediaList = if (!newsDoc.hmidocfilename.isNullOrBlank())
            listOf(Media(uri = newsDoc.hmidocfilename, type = getFileType(newsDoc.hmidocfilename)))
        else emptyList()
        return mediaList.plus(newsDocAdr.map {
            Media(uri = it.docadrfile, type = getFileType(it.docadrfile))
        })
    }

    private fun getFileType(filename: String): MediaType =
        when (filename.substringAfterLast('.', "").lowercase()) {
            "pdf" -> MediaType.PDF
            "jpg", "png" -> MediaType.IMAGE
            else -> MediaType.OTHER
        }

}