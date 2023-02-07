package no.nav.hm.grunndata.db.product

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class AttributeTagService(private val bestillingsordning: Bestillingsordning) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AttributeTagService::class.java)
    }
    fun addBestillingsordningAttribute(entity: Product): Product =
        entity.hmsartNr?.let {
            if (bestillingsordning.isBestillingsordning(entity.hmsartNr)) {
                LOG.debug("Got product in bestillingsordning ${entity.hmsartNr}")
                entity.copy(attributes = entity.attributes.plus(AttributeNames.bestillingsordning to true))
            }
            else
                entity.copy(attributes = entity.attributes.filterNot { it.key == AttributeNames.bestillingsordning })
        } ?: entity


}