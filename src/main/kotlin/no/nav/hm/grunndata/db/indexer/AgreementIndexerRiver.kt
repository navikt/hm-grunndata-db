package no.nav.hm.grunndata.db.indexer

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.River
import no.nav.hm.grunndata.db.agreement.AgreementDocumentDTO
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class AgreementIndexerRiver(river: RiverHead, private val objectMapper: ObjectMapper,
                            private val agreementIndexer: AgreementIndexer): River.PacketListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexerRiver::class.java)
    }

    init {
        river
            .validate { it.demandValue("payloadType", AgreementDocumentDTO::class.java.simpleName)}
            .validate { it.demandKey("payload")}
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val dto = objectMapper.treeToValue(packet["payload"], AgreementDocumentDTO::class.java)
        LOG.info("indexing agreement ${dto.agreement.id}")
        agreementIndexer.index(dto.toDoc())
    }

}