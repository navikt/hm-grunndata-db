package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.*
import no.nav.hm.grunndata.db.series.Series
import no.nav.hm.grunndata.db.series.SeriesService
import no.nav.hm.grunndata.rapid.dto.*
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class ProductRegistrationRiver(
    river: RiverHead,
    private val objectMapper: ObjectMapper,
    private val seriesService: SeriesService,
    private val productService: ProductService
) : River.PacketListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductRegistrationRiver::class.java)
    }

    init {
        LOG.info("Using rapid dto version: $rapidDTOVersion")
        river
            .validate { it.demandValue("eventName", EventName.registeredProductV1) }
            .validate { it.demandKey("payload") }
            .validate { it.demandKey("eventId") }
            .validate { it.demandKey("dtoVersion") }
            .validate { it.demandKey("createdTime") }
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val eventId = packet["eventId"].asText()
        val dtoVersion = packet["dtoVersion"].asLong()
        val createdTime = packet["createdTime"].asLocalDateTime()
        if (dtoVersion > rapidDTOVersion) LOG.warn("dto version $dtoVersion is newer than $rapidDTOVersion")
        val dto = objectMapper.treeToValue(packet["payload"], ProductRegistrationRapidDTO::class.java)
        LOG.info(
            "got product registration id: ${dto.id} supplierId: ${dto.productDTO.supplier.id} supplierRef: ${dto.productDTO.supplierRef} " +
                    "eventId $eventId eventTime: $createdTime adminStatus: ${dto.adminStatus} status: ${dto.productDTO.status} " +
                    "createdBy: ${dto.createdBy} identifier: ${dto.productDTO.identifier}"
        )
        runBlocking {
            if (dto.adminStatus == AdminStatus.APPROVED && dto.draftStatus == DraftStatus.DONE) {
                // series and products need to be merged before sending down the river
                val riverProduct = dto.productDTO.toEntity()
                seriesService.findById(riverProduct.seriesUUID!!)?.let { series ->
                    val mergedProduct = riverProduct.copy(
                        title = series.title,
                        attributes = riverProduct.attributes.copy(
                            text = series.text,
                            url = series.seriesData?.attributes?.url,
                            keywords = series.seriesData?.attributes?.keywords?.toList()
                        ),
                        isoCategory = series.isoCategory,
                        seriesIdentifier = series.identifier,
                        media = series.seriesData?.media ?: riverProduct.media,
                    )
                    productService.saveAndPushTokafka(mergedProduct, EventName.syncedRegisterProductV1)
                } ?: run {
                    LOG.warn("Series not found ${riverProduct.seriesUUID}, saving series for product ${riverProduct.id}")
                    seriesService.saveAndPushTokafka(
                        Series(
                            id = riverProduct.seriesUUID,
                            supplierId = riverProduct.supplierId,
                            identifier = riverProduct.seriesIdentifier?:riverProduct.seriesUUID.toString(),
                            title = riverProduct.title,
                            text = riverProduct.attributes.text?:"",
                            isoCategory = riverProduct.isoCategory,
                            seriesData = SeriesData(media = riverProduct.media),
                            createdBy = riverProduct.createdBy,
                            updatedBy = riverProduct.updatedBy,
                            created = riverProduct.created,
                            updated = riverProduct.updated
                        ), eventName = EventName.syncedRegisterSeriesV1
                    )
                    productService.saveAndPushTokafka(riverProduct, EventName.syncedRegisterProductV1)
                }
            }
        }
    }
}

