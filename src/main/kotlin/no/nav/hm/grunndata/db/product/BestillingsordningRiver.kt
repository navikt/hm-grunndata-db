package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.*
import no.nav.hm.grunndata.rapid.dto.BestillingsordningRegistrationRapidDTO
import no.nav.hm.grunndata.rapid.dto.BestillingsordningStatus
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import no.nav.hm.rapids_rivers.micronaut.deadletter.DeadLetterSupport
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class BestillingsordningRiver(
    river: RiverHead,
    private val objectMapper: ObjectMapper,
    private val productService: ProductService
) : River.PacketListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(BestillingsordningRiver::class.java)
    }

    init {
        river
            .validate { it.demandValue("eventName", EventName.registeredBestillingsordningV1) }
            .validate { it.demandKey("payload") }
            .validate { it.demandKey("eventId") }
            .validate { it.demandKey("dtoVersion") }
            .validate { it.demandKey("createdTime") }
            .register(this)
    }

    @DeadLetterSupport(packet = "packet", messageContext = "context", exceptionsToCatch = 3)
    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val eventId = packet["eventId"].asText()
        val createdTime = packet["createdTime"].asLocalDateTime()
        val dtoVersion = packet["dtoVersion"].asLong()
        if (dtoVersion > rapidDTOVersion) LOG.warn("dto version $dtoVersion is newer than $rapidDTOVersion")
        val dto = objectMapper.treeToValue(packet["payload"], BestillingsordningRegistrationRapidDTO::class.java)
        LOG.info("Got Bestillingsordning event for: ${dto.hmsArtNr} with status: ${dto.status} eventId $eventId eventTime: $createdTime")
        runBlocking {
            val product = productService.findByHmsArtNr(dto.hmsArtNr)
            if (product != null) {
                if (dto.status == BestillingsordningStatus.ACTIVE) {
                    productService.saveAndPushTokafka(
                        product.copy(
                            attributes = product.attributes.copy(
                                bestillingsordning = true
                            )
                        ).toEntity(), EventName.syncedRegisterProductV1, skipUpdateProductAttribute = false
                    )
                } else {
                    productService.saveAndPushTokafka(
                        product.copy(
                            attributes = product.attributes.copy(
                                bestillingsordning = false
                            )
                        ).toEntity(), EventName.syncedRegisterProductV1, skipUpdateProductAttribute = false
                    )
                }
            }
        }
    }
}
