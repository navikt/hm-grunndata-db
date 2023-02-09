package no.nav.hm.grunndata.db.product

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.rapid.EventNames
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import no.nav.hm.grunndata.db.supplier.toDTO
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.slf4j.LoggerFactory
import java.util.*
import javax.transaction.Transactional
import kotlinx.coroutines.async


@Singleton
open class ProductService(
    private val productRepository: ProductRepository,
    private val rapidPushService: RapidPushService,
    private val attributeTagService: AttributeTagService,
    private val supplierRepository: SupplierRepository
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductService::class.java)
    }

    @Transactional
    open suspend fun saveAndPushTokafka(dto: ProductDTO): ProductDTO {
        val product = attributeTagService.addBestillingsordningAttribute(dto).toEntity()
        val saved = (if (product.createdBy == HMDB) productRepository.findByIdentifier(product.identifier)
        else productRepository.findById(product.id))?.let { inDb ->
            productRepository.update(product.copy(id = inDb.id, created = inDb.created,
                createdBy = inDb.createdBy))
        } ?: productRepository.save(product)
        LOG.info("saved hmsArtnr ${saved.hmsArtNr}")
        rapidPushService.pushToRapid(
            key = "${EventNames.hmdbproductsync}-${saved.id}",
            eventName = EventNames.hmdbproductsync, payload = saved.toDTO()
        )
        return saved.toDTO()
    }

    @Transactional
    open suspend fun findById(id: UUID): ProductDTO? = productRepository.findById(id)?.let { it.toDTO() }

    suspend fun Product.toDTO():ProductDTO =  ProductDTO (
        id = id, supplier = supplierRepository.findById(supplierId)!!.toDTO(), title = title, attributes=attributes, status = status, hmsArtNr = hmsArtNr,
        identifier = identifier, supplierRef=supplierRef, isoCategory=isoCategory, accessory=accessory, sparePart=sparePart,
        seriesId=seriesId, techData=techData, media= media, created=created, updated=updated, published=published, expired=expired,
        agreementInfo = agreementInfo, hasAgreement = (agreementInfo!=null), createdBy=createdBy, updatedBy=updatedBy
    )


}