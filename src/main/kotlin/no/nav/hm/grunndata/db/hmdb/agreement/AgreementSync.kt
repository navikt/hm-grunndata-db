package no.nav.hm.grunndata.db.hmdb.agreement

import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.agreement.Agreement
import no.nav.hm.grunndata.db.agreement.AgreementIdDTO
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.agreement.toDTO
import no.nav.hm.grunndata.db.hmdb.*
import no.nav.hm.grunndata.db.hmdb.product.HmDbIdentifier
import no.nav.hm.grunndata.db.hmdbMediaUrl
import no.nav.hm.grunndata.rapid.dto.*
import no.nav.hm.grunndata.rapid.event.EventName
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Singleton
@Requires(bean = KafkaRapid::class)
class AgreementSync(
    private val agreementService: AgreementService,
    private val hmDbClient: HmDbClient,
    private val hmdbBatchRepository: HmDbBatchRepository,
    private val gdbRapidPushService: GdbRapidPushService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementSync::class.java)
    }

    suspend fun syncAgreements() {
        // syncFrom is not supported in old HMDB
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
                val dto = agreementService.findByIdentifier(agreement.identifier)?.let {
                    agreementService.update(agreement.copy(id = it.id, created = it.created)).toDTO()
                } ?: agreementService.save(agreement).toDTO()
                gdbRapidPushService.pushDTOToKafka(dto, EventName.hmdbagreementsyncV1)
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
        status = if (newsDTO.newsexpire.isBefore(LocalDateTime.now())) AgreementStatus.INACTIVE else AgreementStatus.ACTIVE,
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
            description = it.newsDoc.hmidocdesc,
            media = mapMedia(it.newsDoc, it.newsDocAdr)
        )
    }


    private fun mapMedia(newsDoc: NewsDocDTO, newsDocAdr: List<NewsDocAdr>): List<MediaInfo> {
        val mediaList = if (!newsDoc.hmidocfile.isNullOrBlank())
            listOf(
                MediaInfo(uri = "hmidocfiles/${newsDoc.hmidocfile}",
                sourceUri = "$hmdbMediaUrl/hmidocfiles/${newsDoc.hmidocfile}", type = getFileType(newsDoc.hmidocfile),
                text = newsDoc.hmidoctitle, updated = newsDoc.hmidocindate)
            )
        else emptyList()
        return mediaList.plus(newsDocAdr.map {
            MediaInfo(uri = "doclevfiles/${it.docadrfile}", sourceUri = "$hmdbMediaUrl/doclevfiles/${it.docadrfile}",
                type = getFileType(it.docadrfile), text = newsDoc.hmidoctitle, updated = it.docadrindate)
        }.filter { it.type != MediaType.OTHER })
    }

    private fun getFileType(filename: String): MediaType =
        when (filename.substringAfterLast('.', "").lowercase()) {
            "pdf" -> MediaType.PDF
            "jpg", "png" -> MediaType.IMAGE
            "xls", "xlsx" -> MediaType.XLS
            else -> {
                LOG.error("Got unknown media attachment from agreement with $filename")
                MediaType.OTHER
            }
        }

    suspend fun syncDeletedAgreementIds(): List<AgreementIdDTO> {
        val toBeDeleted = findToBeDeletedAgreementIds()
        toBeDeleted.forEach {
            agreementService.findById(it.id)?.let { inDb ->
                agreementService.saveAndPushTokafka(inDb.copy(status = AgreementStatus.INACTIVE, updated = LocalDateTime.now()), EventName.hmdbagreementsyncV1)
            }
        }
        return toBeDeleted
    }

    private suspend fun findToBeDeletedAgreementIds(): List<AgreementIdDTO> {
        val activeIds = agreementService.findIdsByStatus(AgreementStatus.ACTIVE)
        val hmdbIds = hmDbClient.fetchAgreementsIdActive()?.map { "$HMDB-$it" }?.toSet()
        hmdbIds?.let {
            LOG.info("Got ${hmdbIds.size} active ids")
            val toBeDeleted = activeIds.filterNot { hmdbIds.contains(it.identifier) }
            LOG.info("Found ${toBeDeleted.size} to be deleted")
            return toBeDeleted
        }
        return emptyList()
    }
}
