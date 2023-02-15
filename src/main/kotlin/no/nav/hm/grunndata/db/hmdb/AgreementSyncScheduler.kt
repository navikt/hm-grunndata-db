package no.nav.hm.grunndata.db.hmdb

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.hm.grunndata.db.LeaderElection
import no.nav.hm.grunndata.db.agreement.*
import no.nav.hm.grunndata.db.hmdb.agreement.*
import no.nav.hm.grunndata.db.rapid.EventNames
import no.nav.hm.grunndata.dto.AgreementAttachment
import no.nav.hm.grunndata.dto.AgreementPost
import no.nav.hm.grunndata.dto.Media
import no.nav.hm.grunndata.dto.MediaType
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Singleton
@Requires(bean = KafkaRapid::class)
@Requires(property = "schedulers.enabled", value = "true")
class AgreementSyncScheduler(private val agreementRepository: AgreementRepository,
                             private val hmDbClient: HmDbClient,
                             private val hmdbBatchRepository: HmDbBatchRepository,
                             private val rapidPushService: RapidPushService,
                             private val leaderElection: LeaderElection
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementSyncScheduler::class.java)
    }

    @Scheduled(cron="0 30 0 * * *")
    fun syncAgreements() {
        if (leaderElection.isLeader()) {

            runBlocking {
                val syncBatchJob = hmdbBatchRepository.findByName(SYNC_AGREEMENTS) ?: hmdbBatchRepository.save(
                    HmDbBatch(
                        name = SYNC_AGREEMENTS,
                        syncfrom = LocalDateTime.now().minusYears(10).truncatedTo(ChronoUnit.SECONDS)
                    )
                )
                hmDbClient.fetchAgreements()?.let { hmdbagreements ->
                    LOG.info("Calling agreement sync, got total of ${hmdbagreements.size} agreements")
                    val agreements = hmdbagreements.map { it.toAgreement() }
                    runBlocking {
                        agreements.forEach { agreement ->
                            val dto = agreementRepository.findByIdentifier(agreement.identifier)?.let {
                                it.toDTO()
                            } ?: agreementRepository.save(agreement).toDTO()

                            rapidPushService.pushToRapid(
                                key = "${EventNames.hmdbagreementsync}-${dto.id}",
                                eventName = EventNames.hmdbagreementsync, payload = dto
                            )
                        }
                        hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = agreements.last().updated))
                    }
                }
            }
        }
    }

    private fun AvtalePostDTO.toAgreementPost(): AgreementPost = AgreementPost(
        identifier = "$apostid".HmDbIdentifier(),
        nr = apostnr,
        title = aposttitle,
        description = apostdesc
    )


    private fun HmDbAgreementDTO.toAgreement(): Agreement = Agreement (
        identifier = "${newsDTO.newsid}".HmDbIdentifier(),
        title= newsDTO.newstitle,
        resume= newsDTO.newsresume,
        text = newsDTO.newstext,
        published = newsDTO.newspublish,
        expired = newsDTO.newsexpire,
        reference = newsDTO.externid,
        attachments =  mapNewsDocHolder(newsDocHolder),
        posts = poster.map { it.toAgreementPost() }
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