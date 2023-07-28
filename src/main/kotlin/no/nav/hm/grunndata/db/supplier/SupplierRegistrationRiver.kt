package no.nav.hm.grunndata.db.supplier

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.*
import no.nav.hm.grunndata.db.agreement.AgreementRegistrationRiver
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.agreement.toEntity
import no.nav.hm.grunndata.rapid.dto.AgreementRegistrationRapidDTO
import no.nav.hm.grunndata.rapid.dto.DraftStatus
import no.nav.hm.grunndata.rapid.dto.SupplierDTO
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class SupplierRegistrationRiver(river: RiverHead,
                                private val objectMapper: ObjectMapper,
                                private val supplierService: SupplierService
): River.PacketListener  {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierRegistrationRiver::class.java)
    }

    init {
        LOG.info("Using rapid dto version: $rapidDTOVersion")
        river
            .validate { it.demandValue("eventName", EventName.registeredSupplierV1)}
            .validate { it.demandKey("payload")}
            .validate { it.demandKey("eventId")}
            .validate { it.demandKey("dtoVersion")}
            .validate { it.demandKey( "createdTime")}
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val dtoVersion = packet["dtoVersion"].asLong()
        val eventId = packet["eventId"].asText()
        val createdTime = packet["createdTime"].asLocalDateTime()
        if (dtoVersion > rapidDTOVersion) LOG.warn("dto version $dtoVersion is newer than $rapidDTOVersion")
        val dto = objectMapper.treeToValue(packet["payload"], SupplierDTO::class.java)
        LOG.info("got supplier registration id: ${dto.id} eventId $eventId eventTime: $createdTime supplierStatus: ${dto.status}")
        runBlocking {
                supplierService.saveAndPushTokafka(dto.toEntity(), EventName.syncedRegisterSupplierV1)
        }
    }

}
