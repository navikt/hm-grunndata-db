package no.nav.hm.grunndata.db.product

import jakarta.inject.Singleton
import no.nav.hm.grunndata.rapid.dto.AttributeNames
import no.nav.hm.grunndata.rapid.dto.ProductDTO
import org.slf4j.LoggerFactory

@Singleton
class AttributeTagService(private val bestillingsordning: Bestillingsordning) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AttributeTagService::class.java)
    }
    fun addBestillingsordningAttribute(dto: ProductDTO): ProductDTO =
        dto.hmsArtNr?.let {
            if (bestillingsordning.isBestillingsordning(dto.hmsArtNr!!)) {
                LOG.debug("Got product in bestillingsordning ${dto.hmsArtNr}")
                dto.copy(attributes = dto.attributes.plus(AttributeNames.bestillingsordning to true))
            }
            else
                dto.copy(attributes = dto.attributes.filterNot { it.key == AttributeNames.bestillingsordning })
        } ?: dto


}