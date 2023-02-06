package no.nav.hm.grunndata.db.product

import io.micronaut.data.exceptions.DataAccessException
import jakarta.inject.Singleton
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
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
        val entity = attributeTagService.addBestillingsordningAttribute(dto.toEntity())
        val saved = (if (entity.createdBy == HMDB) productRepository.findByIdentifier(entity.identifier)
        else productRepository.findById(entity.id))?.let { inDb ->
            productRepository.update(entity.copy(id = inDb.id, created = inDb.created))
        } ?: productRepository.save(entity)
        rapidPushService.pushToRapid(
            key = "${EventNames.hmdbproductsync}-${saved.id}",
            eventName = EventNames.hmdbproductsync, payload = saved.toDTO()
        )
        return saved.toDTO()
    }

}