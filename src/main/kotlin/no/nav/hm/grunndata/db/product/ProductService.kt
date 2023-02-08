package no.nav.hm.grunndata.db.product

import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.rapid.EventNames
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.slf4j.LoggerFactory
import javax.transaction.Transactional


@Singleton
open class ProductService(
    private val productRepository: ProductRepository,
    private val rapidPushService: RapidPushService,
    private val attributeTagService: AttributeTagService
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

}