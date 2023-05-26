package no.nav.hm.grunndata.db.product

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class AttributeTagService(private val bestillingsordning: Bestillingsordning) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AttributeTagService::class.java)
    }
    fun addBestillingsordningAttribute(dto: Product): Product =
        dto.hmsArtNr?.let {
            if (bestillingsordning.isBestillingsordning(dto.hmsArtNr!!)) {
                LOG.debug("Got product in bestillingsordning ${dto.hmsArtNr}")
                dto.copy(attributes = dto.attributes.copy(bestillingsordning=true))
            }
            else
                dto.copy(attributes = dto.attributes.copy(bestillingsordning=false))
        } ?: dto


}
