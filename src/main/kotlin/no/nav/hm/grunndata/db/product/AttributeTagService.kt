package no.nav.hm.grunndata.db.product

import jakarta.inject.Singleton

@Singleton
class AttributeTagService(private val bestillingsordning: Bestillingsordning) {

    fun addBestillingsordningAttribute(entity: Product): Product =
        entity.HMSArtNr?.let {
            if (bestillingsordning.isBestillingsordning(entity.HMSArtNr))
                entity.copy(attributes = entity.attributes.plus(AttributeNames.bestillingsordning to true))
            else
                entity.copy(attributes = entity.attributes.filterNot { it.key == AttributeNames.bestillingsordning })
        } ?: entity


}