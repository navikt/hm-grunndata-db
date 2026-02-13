package no.nav.hm.grunndata.db.servicejob

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import java.util.UUID
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.River
import no.nav.hm.grunndata.rapid.dto.ServiceJobRapidDTO
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.grunndata.rapid.event.RapidApp
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class ServiceJobRiver(
    river: RiverHead,
    private val objectMapper: ObjectMapper,
    private val serviceJobService: ServiceJobService
) : River.PacketListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(ServiceJobRiver::class.java)
    }

    init {
        river
            .validate { it.demandValue("createdBy", RapidApp.grunndata_register) }
            .validate { it.demandValue("eventName", EventName.registeredServiceJobV1) }
            .validate { it.demandKey("payload") }
            .validate { it.demandKey("dtoVersion") }
            .validate { it.demandKey("createdTime") }
        river.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val dtoVersion = packet["dtoVersion"].asLong()
        if (dtoVersion > rapidDTOVersion) LOG.warn("dto version $dtoVersion is newer than $rapidDTOVersion")
        val serviceJobRapidDTO = objectMapper.treeToValue(packet["payload"], ServiceJobRapidDTO::class.java)
        LOG.info("Got service job registration id: ${serviceJobRapidDTO.id} title: ${serviceJobRapidDTO.title}")
        runBlocking {
            if (serviceJobRapidDTO.id == UUID.fromString("5197bfb3-74b8-4f5b-8d62-1d7735aa8ece")) {
                return@runBlocking
            }
            serviceJobService.saveAndIndex(serviceJobRapidDTO.toEntity())
        }
    }
}

fun ServiceJobRapidDTO.toEntity() = ServiceJob(
    id = id,
    title = title,
    supplierId = supplierId,
    supplierRef = supplierRef,
    hmsArtNr = hmsNr,
    isoCategory = isoCategory,
    published = published,
    expired = expired,
    updated = updated,
    status = status,
    created = created,
    updatedBy = updatedBy,
    createdBy = createdBy,
    attributes = attributes,
    agreements = agreements
)
