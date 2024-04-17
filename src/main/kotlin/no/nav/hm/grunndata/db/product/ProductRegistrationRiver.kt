package no.nav.hm.grunndata.db.product

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
class ProductRegistrationRiver(river: RiverHead,
                               private val objectMapper: ObjectMapper,
                               private val productService: ProductService): River.PacketListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductRegistrationRiver::class.java)
    }

    init {
        LOG.info("Using rapid dto version: $rapidDTOVersion")
        river
            .validate { it.demandValue("eventName", EventName.registeredProductV1)}
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
        val dto = objectMapper.treeToValue(packet["payload"], ProductRegistrationRapidDTO::class.java)
        LOG.info("got product registration id: ${dto.id} supplierId: ${dto.productDTO.supplier.id} supplierRef: ${dto.productDTO.supplierRef} " +
                "eventId $eventId eventTime: $createdTime adminStatus: ${dto.adminStatus} status: ${dto.productDTO.status} " +
                "createdBy: ${dto.createdBy} identifier: ${dto.productDTO.identifier}")
        runBlocking {
            try {
                if (dto.adminStatus == AdminStatus.APPROVED && dto.draftStatus == DraftStatus.DONE)
                    productService.saveAndPushTokafka(dto.productDTO.toEntity(), EventName.syncedRegisterProductV1)
            }
            catch (e: Exception) {
                LOG.error("Failed to save product", e)
            }
        }
    }

}
