package no.nav.hm.grunndata.db.series

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.*
import no.nav.hm.grunndata.rapid.dto.*
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class SeriesRegistrationRiver(river: RiverHead,
                              private val objectMapper: ObjectMapper,
                              private val seriesService: SeriesService): River.PacketListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(SeriesRegistrationRiver::class.java)
    }

    init {
        LOG.info("Using rapid dto version: $rapidDTOVersion")
        river
            .validate { it.demandValue("eventName", EventName.registeredSeriesV1)}
            .validate { it.demandKey("payload")}
            .validate { it.demandKey("eventId")}
            .validate { it.demandKey("dtoVersion")}
            .validate { it.demandKey( "createdTime")}
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val eventId = packet["eventId"].asText()
        val dtoVersion = packet["dtoVersion"].asLong()
        val createdTime = packet["createdTime"].asLocalDateTime()
        if (dtoVersion > rapidDTOVersion) LOG.warn("dto version $dtoVersion is newer than $rapidDTOVersion")
        val dto = objectMapper.treeToValue(packet["payload"], SeriesRegistrationRapidDTO::class.java)
        LOG.info("got series registration id: ${dto.id} eventId $eventId eventTime: $createdTime")
        runBlocking {
            if (dto.draftStatus == DraftStatus.DONE)
                seriesService.saveAndPushTokafka(dto.toEntity(), EventName.syncedRegisterSeriesV1)
        }
    }

}
