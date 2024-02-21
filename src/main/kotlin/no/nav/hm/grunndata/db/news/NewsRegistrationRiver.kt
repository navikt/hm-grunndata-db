package no.nav.hm.grunndata.db.news

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.*
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.NewsDTO
import no.nav.hm.grunndata.rapid.dto.NewsRegistrationRapidDTO
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.grunndata.rapid.event.RapidApp
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class NewsRegistrationRiver(river: RiverHead, private val objectMapper: ObjectMapper,
                            private val rapidPushService: GdbRapidPushService,
                            private val newsService: NewsService): River.PacketListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(NewsRegistrationRiver::class.java)
    }

    init {
        river
            .validate{ it.demandValue("createdBy", RapidApp.grunndata_register) }
            .validate{ it.demandValue("eventName", EventName.registeredNewsV1) }
            .validate{ it.demandKey("payload") }
            .validate{ it.demandKey("dtoVersion") }
            .validate{ it.demandKey("createdTime") }
        river.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val dtoVersion = packet["dtoVersion"].asLong()
        val eventId = packet["eventId"].asText()
        val createdTime = packet["createdTime"].asLocalDateTime()
        if (dtoVersion > rapidDTOVersion) LOG.warn("dto version $dtoVersion is newer than $rapidDTOVersion")
        val dto = objectMapper.treeToValue(packet["payload"], NewsRegistrationRapidDTO::class.java)
        LOG.info("Got news registration id: ${dto.id} title: ${dto.title}")
        runBlocking {
            val saved = newsService.findById(dto.id)?.let {inDb ->
                newsService.update(inDb.copy(title = dto.title,
                    text = dto.text,
                    status = dto.status,
                    published = dto.published,
                    expired = dto.expired,
                    author = dto.author))
            } ?: newsService.save(
                NewsDTO(
                id = dto.id,
                identifier = dto.id.toString(),
                title = dto.title,
                text = dto.text,
                status = dto.status,
                published = dto.published,
                expired = dto.expired,
                created = dto.created,
                updated = dto.updated,
                createdBy = "REGISTER",
                updatedBy = "REGISTER",
                author = dto.author
            ))
            rapidPushService.pushDTOToKafka(saved, EventName.hmdbnewsyncV1)
        }
    }
}