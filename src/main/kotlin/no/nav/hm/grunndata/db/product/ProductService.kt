package no.nav.hm.grunndata.db.product

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import jakarta.inject.Singleton
import jakarta.persistence.criteria.Predicate
import jakarta.transaction.Transactional
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.index.external_product.toExternalDoc
import no.nav.hm.grunndata.db.index.item.IndexItemService
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.product.toDoc
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.db.supplier.toDTO
import no.nav.hm.grunndata.rapid.dto.AgreementInfo
import no.nav.hm.grunndata.rapid.dto.ProductAgreementStatus
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
    private val agreementService: AgreementService,
    private val indexItemService: IndexItemService,
    private val isoCategoryService: IsoCategoryService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductService::class.java)
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
        } else {
            gdbRapidPushService.pushDTOToKafka(productDTO, eventName)
            indexItemService.saveIndexItem(productDTO.toDoc(isoCategoryService), IndexType.PRODUCT)
            // external product
            indexItemService.saveIndexItem(productDTO.toExternalDoc(isoCategoryService), IndexType.EXTERNAL_PRODUCT)
        }
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
    open suspend fun findByAgreementId(agreementId: UUID): List<ProductRapidDTO> =
        productRepository.findByAgreementsJson("""[{"id": "$agreementId"}]""").map { it.toDTO() }

    suspend fun Product.toDTO(): ProductRapidDTO =
        ProductRapidDTO(
            id = id,
            partitionKey = seriesUUID.toString(),
            supplier = supplierService.findByIdCached(supplierId)!!.toDTO(),
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
            mainProduct = mainProduct,
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
                val agreement = agreementService.findByIdCached(agree.id)
                if (agreement != null) {
                    LOG.info("Found agreement with ${agreement.id}, looking up for post: ${agree.postId}")
                    val post = if (agree.postId != null) { agreement.posts.find { it.id == agree.postId } } else null
                    if (post == null) {
                        LOG.warn("Post ${agree.postId} not found for agreement ${agreement.id}, skipping")
                        return@mapNotNull null
                    }
                    AgreementInfo(
                        id = agreement.id,
                        title = agreement.title,
                        identifier = agreement.identifier,
                        rank = agree.rank,
                        postNr = agree.postNr,
                        postIdentifier = agree.postIdentifier,
                        postId = post.id,
                        published = agree.published ?: agreement.published,
                        expired = agree.expired ?: agreement.expired,
                        reference = agreement.reference,
                        postTitle = post.title,
                        refNr = post.refNr,
                        mainProduct = agree.mainProduct,
                        sparePart = agree.sparePart,
                        accessory = agree.accessory,
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

    open suspend fun findProducts(criteria: ProductCriteria, pageable: Pageable) :  Page<ProductRapidDTO> = findProducts(
        spec = buildCriteriaSpec(criteria), pageable)

    private fun buildCriteriaSpec(criteria: ProductCriteria): PredicateSpecification<Product>? {
        if (!criteria.isNotEmpty()) return null
        return PredicateSpecification { root, cb ->
            val predicates = mutableListOf<Predicate>()
            criteria.supplierRef?.let { predicates += cb.equal(root.get<String>("supplierRef"), it) }
            criteria.supplierId?.let { predicates += cb.equal(root.get<UUID>("supplierId"), it) }
            criteria.updated?.let { predicates += cb.greaterThanOrEqualTo(root.get("updated"), it) }
            criteria.status?.let { predicates += cb.equal(root.get<ProductStatus>("status"), it) }
            criteria.seriesUUID?.let { predicates += cb.equal(root.get<UUID>("seriesUUID"), it) }
            criteria.isoCategory?.let { predicates += cb.equal(root.get<String>("isoCategory"), it) }
            criteria.accessory?.let { predicates += cb.equal(root.get<Boolean>("accessory"), it) }
            criteria.sparePart?.let { predicates += cb.equal(root.get<Boolean>("sparePart"), it) }
            criteria.excludeIsoCategories?.takeIf { it.isNotEmpty() }?.let { exclude ->
                predicates += cb.not(root.get<String>("isoCategory").`in`(exclude))
            }
            if (predicates.isEmpty()) cb.conjunction() else cb.and(*predicates.toTypedArray())
        }
    }


    @Transactional
    open suspend fun findProducts(spec: PredicateSpecification<Product>?, pageable: Pageable): Page<ProductRapidDTO> =
        productRepository.findAll(spec, pageable).map { runBlocking { it.toDTO() } }



    suspend fun findDeletedStatusUpdatedBefore(dateTime: LocalDateTime): List<Product> =
        productRepository.findByStatusAndUpdatedBefore(ProductStatus.DELETED, dateTime)

    suspend fun deleteProducts(products: List<Product>) {
        products.forEach { product ->
            indexItemService.saveIndexItem(product.toDTO().toDoc(isoCategoryService), IndexType.PRODUCT)
            indexItemService.saveIndexItem(product.toDTO().toExternalDoc(isoCategoryService), IndexType.EXTERNAL_PRODUCT)
            productRepository.delete(product)
            LOG.info("Product: ${product.id} hmsnr: ${product.hmsArtNr} supplierRef: ${product.supplierRef} was marked for deletion")
        }
    }


    suspend fun findByHmsArtNr(hmsArtNr: String): ProductRapidDTO? = productRepository.findByHmsArtNr(hmsArtNr)?.toDTO()

    suspend fun findBySeriesUUID(seriesUUID: UUID): List<Product> = productRepository.findBySeriesUUID(seriesUUID)
}
