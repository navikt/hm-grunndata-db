package no.nav.hm.grunndata.db.legacy

import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.db.product.Product
import no.nav.hm.grunndata.db.supplier.SupplierService

@Singleton
class LegacyMapper(private val isoCategoryService: IsoCategoryService,
                   private val supplierService: SupplierService) {


    fun Product.toProduktDTO() : ProduktDTO = ProduktDTO(
        adescshort = attributes.shortdescription,
        adraft = false,
        aindate = created.toString(),
        aisapproved = true,
        anbudid = null,
        aout = false,
        aoutdate = expired.toString(),
        apostdesc = agreementInfo?.postTitle,
        apostid = agreementInfo?.postIdentifier,
        apostnr = agreementInfo?.postNr.toString(),
        aposttitle = agreementInfo?.postTitle,
        artid = identifier,
        artname = articleName,
        artno = supplierRef,
        artpostid = null,
        blobfileURL = if (media.isNotEmpty()) media[0].uri else null,
        blobfileURL_snet = null,
        blobtype = if(media.isNotEmpty()) media[0].type.toString() else null,
        blobuse = "1",
        hasanbud = false,
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
}

