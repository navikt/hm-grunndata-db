package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
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
    private val support: ProductAgreementRegistrationRiverSupport,
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
            "got product agreement registration for productId: ${dto.productId} with supplierId: ${dto.supplierId} and supplierRef: ${dto.supplierRef}" +
                    "eventId $eventId eventTime: $createdTime"
        )
        runBlocking {
            productService.findByIdDTO(dto.productId!!)?.let { inDb ->
                try {
                    val product = support.mergeAgreementInProduct(inDb, dto)
                    productService.saveAndPushTokafka(product.toEntity(), EventName.syncedRegisterProductV1)
                }
                catch (e: Exception) {
                    LOG.error("Failed to merge agreement in product ${dto.productId} ", e)
                }
            } ?: run {
                LOG.warn("Product not found for agreement with productId ${dto.productId} supplierId: ${dto.supplierId} and supplierRef: ${dto.supplierRef} skipping")
            }
        }
    }
}

@Singleton
class ProductAgreementRegistrationRiverSupport(private val agreementService: AgreementService) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProductAgreementRegistrationRiverSupport::class.java)
    }

    suspend fun mergeAgreementInProduct(
        product: ProductRapidDTO,
        pag: ProductAgreementRegistrationRapidDTO
    ): ProductRapidDTO {
        val filteredAgreements = product.agreements.filter { it.postId != pag.postId }
        if (pag.status == ProductAgreementStatus.DELETED) return product.copy(agreements = filteredAgreements)

        val hmsNr = if (product.hmsArtNr != pag.hmsArtNr) {
            LOG.info("This product ${product.id} has a different hmsArtNr than the agreement ${pag.hmsArtNr}")
            pag.hmsArtNr
        } else product.hmsArtNr

        val updated = agreementService.findByIdCached(pag.agreementId)?.let { agreementInDb ->
            val foundPost = agreementInDb.posts.find { post -> post.id == pag.postId }
                    ?: throw IllegalStateException("Post ${pag.postId} not found in agreement ${pag.agreementId}, check if agreements are in sync")
            AgreementInfo(
                id = agreementInDb.id,
                identifier = agreementInDb.identifier,
                title = agreementInDb.title,
                rank = pag.rank,
                postNr = pag.post,
                postIdentifier = foundPost.identifier,
                postTitle = foundPost.title,
                postId = pag.postId,
                refNr = foundPost.refNr,
                reference = pag.reference,
                expired = pag.expired,
                status = pag.status,
                published = pag.published,
                mainProduct = pag.mainProduct,
                accessory = pag.accessory,
                sparePart = pag.sparePart,
                articleName = pag.articleName
            )
        } ?: throw IllegalStateException("Agreement ${pag.agreementId} not found, that can not be possible check if agreements are in sync")
        LOG.info("agreements for product ${product.id} updated with agreement ${updated.id} and post ${updated.postId}")
        return product.copy(
            hmsArtNr = hmsNr,
            agreements = filteredAgreements + updated,
            updated = pag.updated,
            updatedBy = pag.updatedBy
        )
    }
}