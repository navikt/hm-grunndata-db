package no.nav.hm.grunndata.db.product

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class AttributeTagService(
    private val digihotSortiment: DigihotSortiment,
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AttributeTagService::class.java)
    }

    fun addBestillingsordningAttribute(product: Product): Product =
        product.hmsArtNr?.let { hmsnr ->
            if (digihotSortiment.isBestillingsordning(hmsnr)) {
                LOG.debug("Got product in bestillingsordning $hmsnr")
                product.copy(attributes = product.attributes.copy(bestillingsordning=true))
            } else product.copy(attributes = product.attributes.copy(bestillingsordning=false))
        } ?: product

    fun addDigitalSoknadAttribute(product: Product): Product =
        product.agreements?.mapNotNull { it.postIdentifier?.removePrefix("HMDB-")?.toIntOrNull() }?.let { apostids ->
            if (apostids.any { digihotSortiment.getApostIdInDigitalCatalog(it) }) {
                LOG.debug("Got product which is digitalSoknad ${product.hmsArtNr}")
                product.copy(attributes = product.attributes.copy(digitalSoknad=true))
            } else product.copy(attributes = product.attributes.copy(digitalSoknad = false))
        } ?: product

    fun addIkkeTilInstitusjonAttribute(product: Product): Product =
        product.isoCategory.let { isoCode ->
            if (digihotSortiment.getIkkeTilInstitusjon(isoCode)) {
                LOG.debug("Got product which is ikkeTilInstitusjon $isoCode")
                product.copy(attributes = product.attributes.copy(ikkeTilInstitusjon=true))
            }
            else product.copy(attributes = product.attributes.copy(ikkeTilInstitusjon=false))
        }

    fun addPakrevdGodkjenningskursAttribute(product: Product): Product =
        product.isoCategory.let { isoCode ->
            digihotSortiment.getPakrevdGodkjenningskurs(isoCode)?.let {
                LOG.debug("Got product with pakrevdGodkjenningskurs=$it for $isoCode")
                product.copy(attributes = product.attributes.copy(pakrevdGodkjenningskurs=it))
            } ?: product
        }

    fun addProdukttypeAttribute(product: Product): Product =
        product.isoCategory.let { isoCode ->
            digihotSortiment.getProdukttype(isoCode)?.let {
                LOG.debug("Got product with produkttype=$it for $isoCode")
                product.copy(attributes = product.attributes.copy(produkttype=it))
            } ?: product
        }
}
