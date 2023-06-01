package no.nav.hm.grunndata.db.product

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class AttributeTagService(private val bestillingsordning: Bestillingsordning) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AttributeTagService::class.java)
    }
    fun addBestillingsordningAttribute(product: Product): Product =
        product.hmsArtNr?.let {
            if (bestillingsordning.isBestillingsordning(product.hmsArtNr!!)) {
                LOG.debug("Got product in bestillingsordning ${product.hmsArtNr}")
                product.copy(attributes = product.attributes.copy(bestillingsordning=true))
            }
            else
                product.copy(attributes = product.attributes.copy(bestillingsordning=false))
        } ?: product


}
