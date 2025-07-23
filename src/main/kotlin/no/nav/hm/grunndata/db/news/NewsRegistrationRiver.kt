package no.nav.hm.grunndata.db.news

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.*
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.index.news.NewsIndexer
import no.nav.hm.grunndata.db.index.news.toDoc

import no.nav.hm.grunndata.rapid.dto.NewsDTO
import no.nav.hm.grunndata.rapid.dto.NewsRegistrationRapidDTO
import no.nav.hm.grunndata.rapid.dto.NewsStatus
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.grunndata.rapid.event.RapidApp
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class NewsRegistrationRiver(river: RiverHead, private val objectMapper: ObjectMapper,
                            private val rapidPushService: GdbRapidPushService,
                            private val newsService: NewsService,
                            private val newsIndexer: NewsIndexer): River.PacketListener {

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
        if (dtoVersion > rapidDTOVersion) LOG.warn("dto version $dtoVersion is newer than $rapidDTOVersion")
        val newsRegistrationDTO = objectMapper.treeToValue(packet["payload"], NewsRegistrationRapidDTO::class.java)
        LOG.info("Got news registration id: ${newsRegistrationDTO.id} title: ${newsRegistrationDTO.title}")
        runBlocking {
            newsService.saveAndPushToKafka(newsRegistrationDTO.toDTO(), EventName.registeredNewsV1)
        }
    }
}

fun NewsRegistrationRapidDTO.toDTO() = NewsDTO(
    id = id ,
    title = title,
    text = text,
    status = status,
    published = published,
    expired = expired ,
    created = created ,
    updated = updated,
    createdBy = createdBy,
    updatedBy = updatedBy,
    identifier = id.toString(),
    author = author
)
