package no.nav.hm.grunndata.db.product

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.REGISTER
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.db.supplier.toDTO
import no.nav.hm.grunndata.rapid.dto.AgreementInfo
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*
import kotlinx.coroutines.coroutineScope
import no.nav.hm.grunndata.rapid.dto.ProductAgreementStatus


@Singleton
open class ProductService(
    private val productRepository: ProductRepository,
    private val attributeTagService: AttributeTagService,
    private val supplierService: SupplierService,
    private val gdbRapidPushService: GdbRapidPushService,
    private val agreementService: AgreementService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductService::class.java)

        // HMDB-5205 = "Cognita", HMDB-5001= "Invacare"
        private val suppliersInRegister: Set<String> = setOf("HMDB-5205", "HMDB-5001")
    }

    @Transactional
    open suspend fun saveAndPushTokafka(
        prod: Product,
        eventName: String,
        skipUpdateProductAttribute: Boolean = false
    ): ProductRapidDTO {
        val product = if (skipUpdateProductAttribute) {
            // Skip the attribute enrichment in certain cases
            prod
        } else {
            // In other cases we need to enrich
            listOf(
                attributeTagService::addBestillingsordningAttribute,
                attributeTagService::addDigitalSoknadAttribute,
                attributeTagService::addSortimentKategoriAttribute,
                attributeTagService::addPakrevdGodkjenningskursAttribute,
                attributeTagService::addProdukttypeAttribute,
            ).fold(prod) { it, enricher -> enricher.call(it) }
        }
        val saved: Product = productRepository.findById(product.id)?.let { inDb ->
            productRepository.update(
                product.copy(
                    id = inDb.id, created = inDb.created,
                    createdBy = inDb.createdBy
                )
            )
        } ?: productRepository.save(product)
        val productDTO = saved.toDTO()
        LOG.info("saved: ${productDTO.id} ${productDTO.supplierRef} ${productDTO.hmsArtNr} ${productDTO.identifier}")
        if (productDTO.title == "Use series title" || productDTO.title.isBlank()) {
            LOG.warn("Product ${productDTO.id} has no title, it means series is not synced yet")
        } else gdbRapidPushService.pushDTOToKafka(productDTO, eventName)
        return productDTO
    }

    @Transactional
    open suspend fun findById(id: UUID): Product? = productRepository.findById(id)


    // TODO, combine two functions when have time :)
    suspend fun findByIdDTO(id: UUID): ProductRapidDTO? = productRepository.findById(id)?.toDTO()

    suspend fun findBySupplierIdAndSupplierRef(supplierId: UUID, supplierRef: String) =
        productRepository.findBySupplierIdAndSupplierRef(supplierId, supplierRef)?.toDTO()

    suspend fun findByIsoCategoryStartsWith(isoCategory: String): List<Product> =
        productRepository.findByIsoCategoryStartsWith(isoCategory)

    suspend fun findByAgreementPostId(agreementPostId: UUID): List<Product> =
        productRepository.findByAgreementsJson("""[{"postId": "$agreementPostId"}]""")

    suspend fun findDistinctIsoCategoryThatHasHmsnr(): Set<String> =
        productRepository.findDistinctIsoCategoryThatHasHmsnr()

    @Transactional
    open suspend fun findByAgreementId(agreementId: UUID): List<Product> =
        productRepository.findByAgreementsJson("""[{"id": "$agreementId"}]""")

    suspend fun Product.toDTO(): ProductRapidDTO =
        ProductRapidDTO(
            id = id,
            partitionKey = seriesUUID.toString(),
            supplier = runBlocking { supplierService.findById(supplierId)!!.toDTO() },
            title = title,
            articleName = articleName,
            attributes = attributes,
            status = status,
            hmsArtNr = hmsArtNr,
            identifier = identifier,
            supplierRef = supplierRef,
            isoCategory = isoCategory,
            accessory = accessory,
            sparePart = sparePart,
            seriesId = seriesId,
            seriesUUID = seriesUUID,
            seriesIdentifier = seriesIdentifier,
            techData = techData,
            media = media,
            created = created,
            updated = updated,
            published = published,
            expired = expired,
            agreementInfo = agreementInfo,
            createdBy = createdBy,
            updatedBy = updatedBy,
            agreements = agreements?.mapNotNull { agree ->
                val agreement = agreementService.findById(agree.id)
                if (agreement != null) {
                    LOG.info("Found agreement with ${agreement.id}, looking up for post: ${agree.postId}")
                    val post = if (agree.postId != null) {
                        agreement.posts.find { it.id == agree.postId }
                            ?: throw IllegalStateException("Post not found for agreement ${agree.id} and post ${agree.postId}")
                    } else null
                    AgreementInfo(
                        id = agreement.id,
                        title = agreement.title,
                        identifier = agreement.identifier,
                        rank = agree.rank,
                        postNr = agree.postNr,
                        postIdentifier = agree.postIdentifier,
                        postId = post?.id,
                        published = agree.published ?: agreement.published,
                        expired = agree.expired ?: agreement.expired,
                        reference = agreement.reference,
                        postTitle = post?.title,
                        refNr = post?.refNr,
                        status = agree.status ?: mapProductAgreemenStatusFromExpired(agree.expired ?: agreement.expired)
                    )
                } else {
                    LOG.warn("Agreement ${agree.id} not found for product ${id}")
                    null
                }
            } ?: emptyList()
        )

    private fun mapProductAgreemenStatusFromExpired(expired: LocalDateTime): ProductAgreementStatus {
        return if (expired.isAfter(LocalDateTime.now())) ProductAgreementStatus.ACTIVE
        else ProductAgreementStatus.INACTIVE
    }

    @Transactional
    open suspend fun findProducts(spec: PredicateSpecification<Product>?, pageable: Pageable): Page<ProductRapidDTO> =
        productRepository.findAll(spec, pageable).map { runBlocking { it.toDTO() } }



    suspend fun findIdsByStatusAndCreatedBy(status: ProductStatus, createdBy: String): List<ProductIdDTO> =
        productRepository.findIdsByStatusAndCreatedBy(status = status, createdBy = createdBy)

    suspend fun findIdsByCreatedBy(createdBy: String): List<ProductIdDTO> =
        productRepository.findIdsByCreatedByAndNotDeleted(createdBy = createdBy)

    suspend fun findByStatusAndExpiredBefore(
        status: ProductStatus,
        expired: LocalDateTime? = LocalDateTime.now()
    ): List<Product> = productRepository.findByStatusAndExpiredBefore(status, expired)

    suspend fun findByHmsArtNr(hmsArtNr: String): ProductRapidDTO? = productRepository.findByHmsArtNr(hmsArtNr)?.toDTO()

    suspend fun findBySeriesUUID(seriesUUID: UUID): List<Product> = productRepository.findBySeriesUUID(seriesUUID)
}

