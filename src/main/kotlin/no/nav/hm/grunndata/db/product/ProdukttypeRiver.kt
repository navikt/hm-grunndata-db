package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.*
import no.nav.hm.grunndata.rapid.dto.Produkttype
import no.nav.hm.grunndata.rapid.dto.ProdukttypeRegistrationRapidDTO
import no.nav.hm.grunndata.rapid.dto.ProdukttypeStatus
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import no.nav.hm.rapids_rivers.micronaut.deadletter.DeadLetterSupport
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class ProdukttypeRiver(
    river: RiverHead,
    private val objectMapper: ObjectMapper,
    private val productService: ProductService
) : River.PacketListener {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProdukttypeRiver::class.java)
    }

    init {
        river
            .validate { it.demandValue("eventName", EventName.registeredProdukttypeV1) }
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
        val dto = objectMapper.treeToValue(packet["payload"], ProdukttypeRegistrationRapidDTO::class.java)
        LOG.info("Got produkttype event for iskode: ${dto.isokode}, produkttype: ${dto.produkttype}, with status: ${dto.status} eventId $eventId eventTime: $createdTime")
        runBlocking {
            val isokode = dto.isokode
            val produkttype = dto.produkttype
            val products = productService.findByIsoCategory(isokode)
            products.forEach { product ->
                if (dto.status == ProdukttypeStatus.ACTIVE) {
                    // TODO: Check with Tuan that there isnt a reason to go ".toDTO()", and back again with ".toEntity()" here,
                    //  like what was done in BestillingsordningRiver.kt. Probably just reuse of an existing ProductService
                    //  function.
                    productService.saveAndPushTokafka(
                        product.copy(
                            attributes = product.attributes.copy(
                                produkttype = Produkttype.valueOf(produkttype),
                            )
                        ), EventName.syncedRegisterProductV1, skipUpdateProductAttribute = true
                    )
                } else {
                    productService.saveAndPushTokafka(
                        product.copy(
                            attributes = product.attributes.copy(
                                produkttype = null,
                            )
                        ), EventName.syncedRegisterProductV1, skipUpdateProductAttribute = true
                    )
                }
            }
        }
    }
}
