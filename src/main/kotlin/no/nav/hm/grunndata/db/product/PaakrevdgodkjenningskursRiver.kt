package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.*
import no.nav.hm.grunndata.rapid.dto.PaakrevdGodkjenningskursRegistrationRapidDTO
import no.nav.hm.grunndata.rapid.dto.PaakrevdGodkjenningskursStatus
import no.nav.hm.grunndata.rapid.dto.PakrevdGodkjenningskurs
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import no.nav.hm.rapids_rivers.micronaut.deadletter.DeadLetterSupport
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class PaakrevdgodkjenningskursRiver(
    river: RiverHead,
    private val objectMapper: ObjectMapper,
    private val productService: ProductService
) : River.PacketListener {
    companion object {
        private val LOG = LoggerFactory.getLogger(PaakrevdgodkjenningskursRiver::class.java)
    }

    init {
        river
            .validate { it.demandValue("eventName", EventName.registeredPaakrevdGodkjenningskursV1) }
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
        val dto = objectMapper.treeToValue(packet["payload"], PaakrevdGodkjenningskursRegistrationRapidDTO::class.java)
        LOG.info("Got paakrevd godkjenningskurs event for iskode: ${dto.isokode}, tittel: ${dto.tittel}, kursId: ${dto.kursId}, with status: ${dto.status} eventId $eventId eventTime: $createdTime")
        runBlocking {
            val isokode = dto.isokode
            val tittel = dto.tittel
            val kursId = dto.kursId
            if (isokode.count() < 4) {
                LOG.error("Unexpectedly short isoCategory prefix in paakrevd godkjenningskurs event, ignoring it, for iskode: ${dto.isokode}, tittel: ${dto.tittel}, kursId: ${dto.kursId}, with status: ${dto.status} eventId $eventId eventTime: $createdTime")
                return@runBlocking
            }
            val products = productService.findByIsoCategoryStartsWith(isokode)
            products.forEach { product ->
                if (dto.status == PaakrevdGodkjenningskursStatus.ACTIVE) {
                    productService.saveAndPushTokafka(
                        product.copy(
                            attributes = product.attributes.copy(
                                pakrevdGodkjenningskurs = PakrevdGodkjenningskurs(tittel = tittel, isokode = isokode, kursId = kursId),
                            )
                        ), EventName.syncedRegisterProductV1, skipUpdateProductAttribute = false
                    )
                } else {
                    productService.saveAndPushTokafka(
                        product.copy(
                            attributes = product.attributes.copy(
                                pakrevdGodkjenningskurs = null,
                            )
                        ), EventName.syncedRegisterProductV1, skipUpdateProductAttribute = false
                    )
                }
            }
        }
    }
}
