package no.nav.hm.grunndata.db.hmdb

import no.nav.hm.grunndata.db.product.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun ProductBatchDTO.toProductList():List<Product> {
    return products.map { productDTO ->
        Product(
            title = productDTO.prodname,
            seriesId = "hmdb-productid-${productDTO.prodid}",
            HMDBArtId = productDTO.artid,
            HMSArtNr = productDTO.stockid,
            isoCategory = productDTO.isocode,
            techData = techdata.getOrDefault(productDTO.artid, emptySet())
                .map {
                    TechData(
                        key = it.techlabeldk!!,
                        value = it.datavalue!!,
                        unit = it.techdataunit!!,
                    )
                }.toSet(),
            supplierId = productDTO.supplier!!.toLong(),
            supplierRef = productDTO.artno ?: productDTO.artid.toString(),
            description = mapDescription(productDTO),
            media = blobs.getOrDefault(productDTO.artid, emptySet()).map{mapBlob(it) },
            created = productDTO.aindate
        )
    }
}

val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun parseDate(aindate: String?): LocalDateTime {
    try {
        val dateStr = aindate!!.substring(0,aindate!!.lastIndexOf("."))
        return LocalDateTime.parse(dateStr, dateTimeFormatter)
    }
    catch (e: DateTimeParseException) {
         println("Could not parse date $aindate")
    }
    return LocalDateTime.now()
}


fun mapBlob(blobDTO: BlobDTO): Media {
    val mediaTytpe = when (blobDTO.blobtype.trim().lowercase()) {
        "billede" -> MediaType.IMAGE
        else -> {
            println(blobDTO.blobtype)
            MediaType.OTHER
        }
    }
    return Media(type = mediaTytpe, uri = blobDTO.blobfile.trim())
}

fun mapDescription(produkt: ProductDTO): Description =
    Description(modelName = produkt.artname,
        modelDescription = produkt.adescshort,
        text = produkt.pshortdesc)

//fun mapAgreement(produkt: ProductDTO): AgreementDTO? {
//    return if (produkt.newsid!=null && null != produkt.newspublish)
//        AgreementDTO(agreementId = produkt.newsid,
//            agreementStart = produkt.newspublish,
//            agreementEnd = produkt.newsexpire!!,
//            agreementPostId = produkt.apostid!!,
//            agreementPostNr = produkt.apostnr!!,
//            agreementPostTitle = produkt.aposttitle!!,
//            agreementRank = produkt.postrank!!) else null
//}

