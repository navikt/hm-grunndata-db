package no.nav.hm.grunndata.db.product

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.db.supplier.toDTO
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*
import javax.transaction.Transactional


@Singleton
open class ProductService(
    private val productRepository: ProductRepository,
    private val attributeTagService: AttributeTagService,
    private val supplierService: SupplierService,
    private val gdbRapidPushService: GdbRapidPushService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductService::class.java)
    }

    @Transactional
    open suspend fun saveAndPushTokafka(dto: Product, eventName: String): ProductRapidDTO {
        val product = attributeTagService.addBestillingsordningAttribute(dto)
        val saved = (if (product.createdBy == HMDB) productRepository.findByIdentifier(product.identifier)
        else productRepository.findById(product.id))?.let { inDb ->
            productRepository.update(product.copy(id = inDb.id, created = inDb.created,
                createdBy = inDb.createdBy))
        } ?: productRepository.save(product)
        val productDTO = saved.toDTO()
        LOG.info("saved hmsArtnr ${productDTO.hmsArtNr}")
        gdbRapidPushService.pushDTOToKafka(productDTO, eventName)
        return productDTO
    }

    @Transactional
    open suspend fun findById(id: UUID): Product? = productRepository.findById(id)

    private fun Product.toDTO():ProductRapidDTO = ProductRapidDTO (
        id = id, supplier = runBlocking{supplierService.findById(supplierId)!!.toDTO()}, title = title, articleName = articleName,  attributes=attributes,
        status = status, hmsArtNr = hmsArtNr, identifier = identifier, supplierRef=supplierRef, isoCategory=isoCategory,
        accessory=accessory, sparePart=sparePart, seriesId=seriesId, techData=techData, media= media, created=created,
        updated=updated, published=published, expired=expired, agreementInfo = agreementInfo, hasAgreement = (agreementInfo!=null),
        createdBy=createdBy, updatedBy=updatedBy
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
        }
    }

    suspend fun findIdsByStatus(status: ProductStatus): List<ProductIdDTO> = productRepository.findIdsByStatus(status = status)
}

