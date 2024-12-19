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

    fun addBestillingsordningAttribute(product: Product): Product {
        if (product.hmsArtNr != null && digihotSortiment.isBestillingsordning(product.hmsArtNr)) {
            LOG.debug("Got product in bestillingsordning ${product.hmsArtNr}")
            return product.copy(attributes = product.attributes.copy(bestillingsordning = true))
        } else {
            return product.copy(attributes = product.attributes.copy(bestillingsordning = false))
        }
    }

    fun addDigitalSoknadAttribute(product: Product): Product {
        val postIds = onlyActiveAgreements(product).mapNotNull { it.postId }
        if (postIds.any { digihotSortiment.getPostIdInDigitalCatalog(it) }) {
            LOG.debug("Got product which is digitalSoknad ${product.hmsArtNr}")
            return product.copy(attributes = product.attributes.copy(digitalSoknad = true))
        } else {
            return product.copy(attributes = product.attributes.copy(digitalSoknad = false))
        }
    }

    fun addSortimentKategoriAttribute(product: Product): Product {
        val postId = onlyActiveAgreements(product).mapNotNull { it.postId }
            .find { postId -> digihotSortiment.getPostIdInDigitalCatalog(postId) }
        if (postId != null) {
            val sortimentKategori = digihotSortiment.getSortimentKategoriByPostIdInDigitalCatalog(postId)!!
            LOG.debug("Got product (hmsnr=${product.hmsArtNr}) with sortimentKategori=$sortimentKategori")
            return product.copy(attributes = product.attributes.copy(sortimentKategori = sortimentKategori))
        } else {
            return product.copy(attributes = product.attributes.copy(sortimentKategori = null))
        }
    }

    fun addPakrevdGodkjenningskursAttribute(product: Product): Product {
        val kurs = digihotSortiment.getPakrevdGodkjenningskurs(product.isoCategory)
        return product.copy(attributes = product.attributes.copy(pakrevdGodkjenningskurs = kurs))
    }

    fun addProdukttypeAttribute(product: Product): Product {
        val type = digihotSortiment.getProdukttype(product.isoCategory)
        LOG.debug("Got product with produkttype=${type} for ${product.isoCategory}")
        return product.copy(attributes = product.attributes.copy(produkttype = type))
    }

    private fun onlyActiveAgreements(product: Product) = (product.agreements ?: listOf())
            .filter { it.published!!.isBefore(java.time.LocalDateTime.now()) }
            .filter { it.expired == null || it.expired.isAfter(java.time.LocalDateTime.now()) }
            .filter { it.status == no.nav.hm.grunndata.rapid.dto.ProductAgreementStatus.ACTIVE }
            .filter { product.status == no.nav.hm.grunndata.rapid.dto.ProductStatus.ACTIVE
        }
}
