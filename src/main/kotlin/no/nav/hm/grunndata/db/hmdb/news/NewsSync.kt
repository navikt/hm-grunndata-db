package no.nav.hm.grunndata.db.hmdb.news

import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.hmdb.HmDbBatch
import no.nav.hm.grunndata.db.hmdb.HmDbBatchRepository
import no.nav.hm.grunndata.db.hmdb.HmDbClient
import no.nav.hm.grunndata.db.hmdb.SYNC_NEWS
import no.nav.hm.grunndata.db.hmdb.product.HmDbIdentifier
import no.nav.hm.grunndata.db.news.NewsService
import no.nav.hm.grunndata.rapid.dto.NewsDTO
import no.nav.hm.grunndata.rapid.dto.NewsStatus
import no.nav.hm.grunndata.rapid.event.EventName
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Singleton
@Requires(bean = KafkaRapid::class)
class NewsSync(private val newsService: NewsService,
               private val hmDbClient: HmDbClient,
               private val hmDbBatchRepository: HmDbBatchRepository,
               private val gdbRapidPushService: GdbRapidPushService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(NewsSync::class.java)
    }

    suspend fun syncNews() {
        val syncBatchJob = hmDbBatchRepository.findByName(SYNC_NEWS) ?: hmDbBatchRepository.save(
            HmDbBatch(
                name = SYNC_NEWS,
                syncfrom = LocalDateTime.now().minusYears(20).truncatedTo(ChronoUnit.SECONDS)
            )
        )
        hmDbClient.fetchNews()?.let { hmDbNews ->
            LOG.info("Calling news sync, got total of ${hmDbNews.size} news")
            val news = hmDbNews.map { it.toNews() }.sortedBy { it.updated }
            news.forEach { it ->
                val dto = newsService.findByIdentifier(it.identifier)?.let {inDb ->
                    newsService.update(it.copy(id = inDb.id, created = inDb.created))
                } ?: newsService.save(it)
                gdbRapidPushService.pushDTOToKafka(dto, EventName.hmdbnewsyncV1)
            }
            hmDbBatchRepository.update(syncBatchJob.copy(syncfrom = news.last().updated))
        }
    }

    private fun HMDNewsDTO.toNews(): NewsDTO {
        val expired = this.newsexpire ?: this.newspublish.plusMonths(3)
        return NewsDTO(
            id = UUID.randomUUID(),
            identifier = "$newsid".HmDbIdentifier(),
            title = this.newstitle,
            text = this.newsresume + "<br/>" + this.newstext,
            status = if (expired.isAfter(LocalDateTime.now())) NewsStatus.ACTIVE else NewsStatus.INACTIVE,
            published = this.newspublish,
            expired = expired,
            createdBy = "HMDB",
            updatedBy = "HMDB",
            created = LocalDateTime.now(),
            updated = LocalDateTime.now(),
            author = "Admin"
        )
    }
}