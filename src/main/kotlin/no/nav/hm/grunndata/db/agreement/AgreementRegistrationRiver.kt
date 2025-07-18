package no.nav.hm.grunndata.db.agreement

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.*
import no.nav.hm.grunndata.db.index.agreement.AgreementIndexer
import no.nav.hm.grunndata.db.index.agreement.toDoc
import no.nav.hm.grunndata.db.product.ProductRegistrationRiver
import no.nav.hm.grunndata.db.product.ProductService
import no.nav.hm.grunndata.db.product.toEntity
import no.nav.hm.grunndata.rapid.dto.*
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class AgreementRegistrationRiver(river: RiverHead,
                                 private val objectMapper: ObjectMapper,
                                 private val agreementService: AgreementService, private val agreementIndexer: AgreementIndexer
): River.PacketListener  {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementRegistrationRiver::class.java)
    }

    init {
        LOG.info("Using rapid dto version: $rapidDTOVersion")
        river
            .validate { it.demandValue("eventName", EventName.registeredAgreementV1)}
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
        val dto = objectMapper.treeToValue(packet["payload"], AgreementRegistrationRapidDTO::class.java)
        LOG.info("got agreement registration id: ${dto.id} eventId $eventId eventTime: $createdTime agreementStatus: ${dto.agreementDTO.status}")
        runBlocking {
            if (dto.draftStatus == DraftStatus.DONE) {
                val agreementDTO = dto.agreementDTO
                agreementService.saveAndPushTokafka(agreementDTO.toEntity(), EventName.syncedRegisterAgreementV1)
                LOG.info("indexing agreement id: ${agreementDTO.id} reference: ${agreementDTO.reference}")
                agreementIndexer.index(agreementDTO.toDoc())
            }
        }
    }

}