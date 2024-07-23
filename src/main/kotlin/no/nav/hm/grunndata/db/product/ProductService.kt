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
import no.nav.hm.grunndata.db.HMDB
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
    open suspend fun saveAndPushTokafka(prod: Product, eventName: String, skipUpdateProductAttribute: Boolean = false): ProductRapidDTO {
        val product = if (skipUpdateProductAttribute) {
            // Skip the attribute enrichment below if we are being updated by hm-grunndata-register
            prod
        } else {
            // When our update comes from hmdb we need to set product attributes here!
            listOf(
                attributeTagService::addBestillingsordningAttribute,
                attributeTagService::addDigitalSoknadAttribute,
                attributeTagService::addSortimentKategoriAttribute,
                attributeTagService::addPakrevdGodkjenningskursAttribute,
                attributeTagService::addProdukttypeAttribute,
            ).fold(prod) { it, enricher -> enricher.call(it) }
        }
        val saved: Product = if (product.updatedBy == HMDB) {
            LOG.info("Got product from HMDB ${product.identifier} hmsnr: ${product.hmsArtNr} supplierId: ${product.supplierId} supplierRef: ${product.supplierRef}")
            productRepository.findByIdentifier(product.identifier)?.let { inDb ->
                if (suppliersInRegister.contains(inDb.identifier) || inDb.updatedBy == REGISTER) { // skip updating from HMDB if product has been modified by register
                    LOG.info("Skipping updating for product identifier: ${product.identifier} id: ${product.id} for supplier: ${inDb.identifier} because updated from register")
                    return inDb.toDTO()
                }
                productRepository.update(product.copy(id = inDb.id, created = inDb.created, createdBy = inDb.createdBy))
            } ?: productRepository.save(product)
        } else productRepository.findBySupplierIdAndSupplierRef(product.supplierId, product.supplierRef)?.let { inDb ->
            productRepository.update(product.copy(id = inDb.id, created = inDb.created,
                createdBy = inDb.createdBy))
        } ?: productRepository.save(product)
        val productDTO = saved.toDTO()
        LOG.info("saved: ${productDTO.id} ${productDTO.supplierRef} ${productDTO.hmsArtNr} ${productDTO.identifier}")
        gdbRapidPushService.pushDTOToKafka(productDTO, eventName)
        return productDTO
    }



    @Transactional
    open suspend fun findById(id: UUID): Product? = productRepository.findById(id)


    // TODO, combine two functions when have time :)
    suspend fun findByIdDTO(id:UUID): ProductRapidDTO? = productRepository.findById(id)?.toDTO()

    suspend fun findBySupplierIdAndSupplierRef(supplierId: UUID, supplierRef: String) = productRepository.findBySupplierIdAndSupplierRef(supplierId, supplierRef)?.toDTO()

    suspend fun findByIsoCategoryStartsWith(isoCategory: String): List<Product> =
        productRepository.findByIsoCategoryStartsWith(isoCategory)

    suspend fun findByAgreementPostId(agreementPostId: UUID): List<Product> =
        productRepository.findByAgreementsJson("""[{"postId": "$agreementPostId"}]""")

    @Transactional
    open suspend fun findByAgreementId(agreementId: UUID): List<Product> =
        productRepository.findByAgreementsJson("""[{"id": "$agreementId"}]""")

    private fun Product.toDTO():ProductRapidDTO = ProductRapidDTO (
        id = id, supplier = runBlocking{supplierService.findById(supplierId)!!.toDTO()},
        title = title, articleName = articleName,  attributes=attributes,
        status = status, hmsArtNr = hmsArtNr, identifier = identifier, supplierRef=supplierRef, isoCategory=isoCategory,
        accessory=accessory, sparePart=sparePart, seriesId=seriesId, seriesUUID = seriesUUID, seriesIdentifier = seriesIdentifier, techData=techData, media= media, created=created,
        updated=updated, published=published, expired=expired, agreementInfo = agreementInfo,
        createdBy=createdBy, updatedBy=updatedBy, agreements = agreements?.map {agree ->
            val agreement = agreementService.findByIdentifier(agree.identifier!!)
            val post = agreement!!.posts.find { it.identifier == agree.postIdentifier }
                ?: throw RuntimeException("Wrong agreement state!, should never happen")
            AgreementInfo(
                id = agreement.id,
                title = agreement.title,
                identifier = agreement.identifier,
                rank = agree.rank,
                postNr = agree.postNr,
                postIdentifier = agree.postIdentifier,
                postId = post.id,
                published = agreement.published,
                expired = agreement.expired,
                reference = agreement.reference,
                postTitle = post.title,
                refNr = post.refNr
            )
        } ?: emptyList()
    )

    @Transactional
    open suspend fun findProducts(params: Map<String, String>?, pageable: Pageable) : Page<ProductRapidDTO> =
        productRepository.findAll(buildCriteriaSpec(params), pageable).map {it.toDTO()}

    private fun buildCriteriaSpec(params: Map<String, String>?): PredicateSpecification<Product>?
            = params?.let {
        where {
            if (params.contains("supplierRef")) root[Product::supplierRef] eq params["supplierRef"]
            if (params.contains("supplierId"))  root[Product::supplierId] eq UUID.fromString(params["supplierId"]!!)
            if (params.contains("updated")) root[Product::updated] greaterThanOrEqualTo LocalDateTime.parse(params["updated"])
            if (params.contains("status")) root[Product::status] eq params["status"]
            if (params.contains("seriesUUID")) root[Product::seriesUUID] eq UUID.fromString(params["seriesUUID"]!!)
        }
    }

    suspend fun findIdsByStatusAndCreatedBy(status: ProductStatus, createdBy: String): List<ProductIdDTO> =
        productRepository.findIdsByStatusAndCreatedBy(status = status, createdBy = createdBy)

    suspend fun findIdsByCreatedBy(createdBy: String): List<ProductIdDTO> =
        productRepository.findIdsByCreatedByAndNotDeleted(createdBy = createdBy)

    suspend fun findByStatusAndExpiredBefore(status: ProductStatus, expired: LocalDateTime? = LocalDateTime.now()): List<Product> = productRepository.findByStatusAndExpiredBefore(status, expired)

    suspend fun findByHmsArtNr(hmsArtNr: String): ProductRapidDTO? = productRepository.findByHmsArtNr(hmsArtNr)?.toDTO()

    suspend fun findBySeriesUUID(seriesUUID: UUID): List<Product> = productRepository.findBySeriesUUID(seriesUUID)
}

