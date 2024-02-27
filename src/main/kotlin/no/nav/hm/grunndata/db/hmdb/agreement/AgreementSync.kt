package no.nav.hm.grunndata.db.hmdb.agreement

import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import jakarta.inject.Singleton
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.agreement.Agreement
import no.nav.hm.grunndata.db.agreement.AgreementIdDTO
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.agreement.toDTO
import no.nav.hm.grunndata.db.hmdb.HmDbBatch
import no.nav.hm.grunndata.db.hmdb.HmDbBatchRepository
import no.nav.hm.grunndata.db.hmdb.HmDbClient
import no.nav.hm.grunndata.db.hmdb.SYNC_AGREEMENTS
import no.nav.hm.grunndata.db.hmdb.product.HmDbIdentifier
import no.nav.hm.grunndata.db.hmdbMediaUrl
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.AgreementAttachment
import no.nav.hm.grunndata.rapid.dto.AgreementPost
import no.nav.hm.grunndata.rapid.dto.AgreementStatus
import no.nav.hm.grunndata.rapid.dto.MediaInfo
import no.nav.hm.grunndata.rapid.dto.MediaType
import no.nav.hm.grunndata.rapid.event.EventName
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Singleton
@Requires(bean = KafkaRapid::class)
class AgreementSync(
    private val agreementService: AgreementService,
    private val hmDbClient: HmDbClient,
    private val hmdbBatchRepository: HmDbBatchRepository,
    private val gdbRapidPushService: GdbRapidPushService,
    private val supplierService: SupplierService,
    @Value("\${media.storage.cdnurl}") private val cdnUrl: String
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
                val dto = agreementService.findByIdentifier(agreement.identifier)?.let { inDb ->
                    agreementService.update(
                        agreement.copy(
                            id = inDb.id, created = inDb.created,
                            posts = mergePosts(inDb.posts, agreement.posts)
                        )
                    ).toDTO()
                } ?: agreementService.save(agreement).toDTO()
                gdbRapidPushService.pushDTOToKafka(dto, EventName.hmdbagreementsyncV1)
            }
            hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = agreements.last().updated))
        }

    }



    private fun AvtalePostDTO.toAgreementPost(): AgreementPost = AgreementPost(
        identifier = "$apostid".HmDbIdentifier(),
        id = UUID.randomUUID(),
        refNr = extractDelkontraktNrFromTitle(aposttitle),
        nr = apostnr,
        title = cleanPostTitle(aposttitle),
        description = apostdesc
    )


    private fun HmDbAgreementDTO.toAgreement(): Agreement = Agreement(
        identifier = "${newsDTO.newsid}".HmDbIdentifier(),
        title = newsDTO.newstitle.trim(),
        resume = newsDTO.newsresume,
        status = if (newsDTO.newsexpire!!.isBefore(LocalDateTime.now()) || newsDTO.newspublish.isAfter(LocalDateTime.now())) AgreementStatus.INACTIVE else AgreementStatus.ACTIVE,
        text = if (newsDTO.newstext != null) cleanUpText(newsDTO.newstext) else null,
        published = newsDTO.newspublish,
        expired = newsDTO.newsexpire ?: LocalDateTime.now().plusYears(3),
        reference = newsDTO.externid!!.trim(),
        attachments = mapNewsDocHolder(newsDocHolder),
        posts = poster.map { it.toAgreementPost() },
        isoCategory = isonumber
    )

    private fun cleanUpText(newstext: String): String =
        newstext.replace("blobs/hmidocfiles/", "$cdnUrl/hmidocfiles/")


    private fun mapNewsDocHolder(newsdocHolder: List<NewsDocHolder>): List<AgreementAttachment> = newsdocHolder.map {
        AgreementAttachment(
            id = UUID.randomUUID(),
            title = it.newsDoc.hmidoctitle,
            description = it.newsDoc.hmidocdesc,
            media = mapMedia(it.newsDoc, it.newsDocAdr)
        )
    }


    private fun mapMedia(newsDoc: NewsDocDTO, newsDocAdr: List<NewsDocAdr>): List<MediaInfo> {
        val mediaList = if (!newsDoc.hmidocfile.isNullOrBlank())
            listOf(
                MediaInfo(
                    uri = "hmidocfiles/${newsDoc.hmidocfile}",
                    sourceUri = "$hmdbMediaUrl/hmidocfiles/${newsDoc.hmidocfile}",
                    type = getFileType(newsDoc.hmidocfile),
                    text = newsDoc.hmidoctitle,
                    updated = newsDoc.hmidocindate
                )
            )
        else emptyList()
        return mediaList.plus(newsDocAdr.map {
            val supplier = supplierService.findByIdentifier("${it.adressid}".HmDbIdentifier())
            MediaInfo(
                uri = "doclevfiles/${it.docadrfile}",
                sourceUri = "$hmdbMediaUrl/doclevfiles/${it.docadrfile}",
                type = getFileType(it.docadrfile),
                text = supplier?.name ?: newsDoc.hmidoctitle,
                updated = it.docadrupdate
            )
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
                agreementService.saveAndPushTokafka(
                    inDb.copy(
                        status = AgreementStatus.INACTIVE,
                        updated = LocalDateTime.now()
                    ), EventName.hmdbagreementsyncV1
                )
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

    suspend fun fixAgreementsInDb() {
        val agreements = agreementService.findAll(spec = null, pageable = Pageable.from(0, 1000))
        // hack save back agreements
        LOG.info("Found agreements ${agreements.size} to be updated")
        agreements.forEach {
            agreementService.update(it)
        }
    }
}

fun mergePosts(inDbPosts: List<AgreementPost>, newPosts: List<AgreementPost>): List<AgreementPost> {
    val notInDbPosts = newPosts.filter { newPost -> inDbPosts.none { it.identifier == newPost.identifier } }
    val toBeDeletedPosts = inDbPosts.filter { inDbPost -> newPosts.none { it.identifier == inDbPost.identifier } }
    val inDbPostsToKeep = inDbPosts.filter { inDbPost -> newPosts.any { it.identifier == inDbPost.identifier } }
    val mergedPosts = inDbPostsToKeep.map { inDbPost ->
        newPosts.find { it.identifier == inDbPost.identifier }
            ?.let { newPost ->
                inDbPost.copy(
                    title = newPost.title,
                    description = newPost.description,
                    nr = newPost.nr,
                    refNr = newPost.refNr
                )
            } ?: inDbPost
    }
    return mergedPosts.plus(notInDbPosts).minus(toBeDeletedPosts)
}

fun cleanPostTitle(aposttitle: String): String {
    val postDelkontraktRegex = "(?i)(Post|Delkontrakt)\\s".toRegex()
    return aposttitle.replace(postDelkontraktRegex, "")
}

fun extractDelkontraktNrFromTitle(title: String): String? {
    val regex = """(\d+)([A-Z]*)([.|:])""".toRegex()
    return regex.find(title)?.groupValues?.get(0)?.dropLast(1)
}
