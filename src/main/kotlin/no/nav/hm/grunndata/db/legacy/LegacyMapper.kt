package no.nav.hm.grunndata.db.legacy

import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.db.product.Product
import no.nav.hm.grunndata.db.supplier.SupplierService
import java.time.LocalDateTime

@Singleton
class LegacyMapper(private val isoCategoryService: IsoCategoryService,
                   private val supplierService: SupplierService) {


    fun Product.toProduktDTO() : ProduktDTO = ProduktDTO(
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
        blobtype = if(media.isNotEmpty()) media[0].type.toString() else null,
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
        supplier = supplierService.findById(supplierId)?.identifier,
    )

    fun Product.toTekniskeDataDTO() : List<TekniskeDataDTO> = techData.map {
        TekniskeDataDTO(
            prodid = seriesId!!,
            artid = identifier,
            datavalue = it.value,
            techdataunit = it.unit,
            techlabeldk = it.key
        )
    }
}

