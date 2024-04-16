package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.*
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.rapid.dto.*
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class ProductAgreementRegistrationRiver(
    river: RiverHead,
    private val objectMapper: ObjectMapper,
    private val agreementService: AgreementService,
    private val productService: ProductService
) : River.PacketListener {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProductAgreementRegistrationRiver::class.java)
    }

    init {
        LOG.info("Using rapid dto version: $rapidDTOVersion")
        river
            .validate { it.demandValue("eventName", EventName.registeredProductAgreementV1) }
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
        val dto = objectMapper.treeToValue(packet["payload"], ProductAgreementRegistrationRapidDTO::class.java)
        LOG.info(
            "got product agreement registration with supplierId: ${dto.supplierId} and supplierRef: ${dto.supplierRef}" +
                    "eventId $eventId eventTime: $createdTime"
        )
        runBlocking {
            productService.findBySupplierIdAndSupplierRef(dto.supplierId, dto.supplierRef)?.let { inDb ->
                try {
                    val product = mergeAgreementInProduct(inDb, dto)
                    productService.saveAndPushTokafka(product.toEntity(), EventName.syncedRegisterProductV1)
                }
                catch (e: Exception) {
                    LOG.error("Failed to merge agreement in product", e)
                }
            }
        }

    }

    private suspend fun mergeAgreementInProduct(
        product: ProductRapidDTO,
        agreement: ProductAgreementRegistrationRapidDTO
    ): ProductRapidDTO {

        val filteredAgreements = product.agreements.filter { it.postId != agreement.postId }
        if (agreement.status == ProductAgreementStatus.DELETED) return product.copy(agreements = filteredAgreements)

        val hmsNr = if (product.hmsArtNr != agreement.hmsArtNr) {
            LOG.info("This product ${product.id} has a different hmsArtNr than the agreement ${agreement.hmsArtNr}")
            agreement.hmsArtNr
        } else product.hmsArtNr

        val updated = agreementService.findById(agreement.agreementId)?.let { agreementInDb ->
            val foundPost = agreementInDb.posts.find { post -> post.id == agreement.postId }
                ?: throw IllegalStateException("Post ${agreement.postId} not found in agreement ${agreement.agreementId}, check if agreements are in sync")
            AgreementInfo(
                id = agreementInDb.id,
                identifier = agreementInDb.identifier,
                title = agreementInDb.title,
                rank = agreement.rank,
                postNr = agreement.post,
                postIdentifier = foundPost.identifier,
                postTitle =foundPost.title,
                postId = agreement.postId,
                refNr = foundPost.refNr,
                reference = agreement.reference,
                expired = agreement.expired,
                published = agreementInDb.published
            )
        } ?: throw IllegalStateException("Agreement ${agreement.agreementId} not found, that can not be possible check if agreements are in sync")
        return product.copy(
            hmsArtNr = hmsNr,
            agreements = filteredAgreements + updated
        )
    }
}