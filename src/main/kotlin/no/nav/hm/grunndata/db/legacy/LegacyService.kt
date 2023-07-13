package no.nav.hm.grunndata.db.legacy

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.db.product.ProductService
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.dto.SupplierDTO
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Singleton
class LegacyService(
    private val isoCategoryService: IsoCategoryService,
    private val supplierService: SupplierService, private val productService: ProductService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(LegacyService::class.java)
    }

    suspend fun retrieveAllProductAndMapToLegacyDTO(): ErstattProdukterDTO {
        val page = productService.findProducts(emptyMap(), Pageable.from(0, 50000, Sort.of(Sort.Order("updated"))))
        if (page.numberOfElements > 0) {
            LOG.info("found products numberOfElements: ${page.numberOfElements}")
            val products = page.content.filter { it.status != ProductStatus.DELETED }
            val produkter = mutableListOf<ProduktDTO>()
            val techdata = mutableListOf<TekniskeDataDTO>()
            products.forEach {
                produkter.add(it.toProduktDTO())
                techdata.addAll(it.toTekniskeDataDTO())
            }
            return ErstattProdukterDTO(produkter, techdata)
        }
        return ErstattProdukterDTO(emptyList(), emptyList())
    }

    suspend fun retriveAllSuppliersAndMapToLegacyDTO(): ErstattLeverandorerDTO {
        val page = supplierService.findSuppliers(emptyMap(), Pageable.unpaged())
        if (page.numberOfElements > 0) {
            LOG.info("found supplier numberOfElements: ${page.numberOfElements}")
            return ErstattLeverandorerDTO( leverandorer = page.content.map {
               it.toLeverandorDTO() }
            )
        }
        return ErstattLeverandorerDTO(leverandorer = emptyList())
    }

    fun ProductRapidDTO.toProduktDTO(): ProduktDTO = ProduktDTO(
        adescshort = attributes.shortdescription,
        adraft = false,
        aindate = created.toString(),
        aisapproved = true,
        anbudid = attributes.tenderId,
        aout = expired.isBefore(LocalDateTime.now()),
        aoutdate = expired.toString(),
        apostdesc = agreementInfo?.postTitle, // mapper ikke dette
        apostid = agreementInfo?.postIdentifier,
        apostnr = agreementInfo?.postNr.toString(),
        aposttitle = agreementInfo?.postTitle,
        artid = identifier,
        artname = articleName,
        artno = supplierRef,
        artpostid = null, //mapper ikke dette
        blobfileURL = if (media.isNotEmpty()) media[0].uri else null,
        blobfileURL_snet = null,
        blobtype = if (media.isNotEmpty()) media[0].type.toString() else null,
        blobuse = "1",
        hasanbud = attributes.hasTender,
        isactive = true,
        isocode = isoCategory,
        isotextshort = isoCategoryService.lookUpCode(isoCategory)!!.isoText,
        isotitle = isoCategoryService.lookUpCode(isoCategory)!!.isoTitle,
        ldbid = "2",
        newsexpire = agreementInfo?.expired?.toLocalDate(),
        newsid = agreementInfo?.identifier,
        newspublish = null,
        pisapproved = true,
        postrank = agreementInfo?.rank?.toLong(),
        prodid = seriesId!!,
        prodname = title,
        pshortdesc = attributes.text!!,
        stockid = hmsArtNr,
        supplier = supplier.identifier,
    )

    fun ProductRapidDTO.toTekniskeDataDTO(): List<TekniskeDataDTO> = techData.map {
        TekniskeDataDTO(
            prodid = seriesId!!,
            artid = identifier,
            datavalue = it.value,
            techdataunit = it.unit,
            techlabeldk = it.key
        )
    }

    fun SupplierDTO.toLeverandorDTO(): LeverandorDTO = LeverandorDTO(
        leverandorid = identifier,
        leverandornavn = name, adresse = info.address, postnummer = info.postNr, poststed = info.postLocation,
        telefon = info.phone, epost = info.email, www = info.homepage, landkode = info.countryCode
    )
}
