package no.nav.hm.grunndata.db.hmdb

import no.nav.hm.grunndata.db.product.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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

fun mapBlobs(blobs: List<BlobDTO>): List<Media> = blobs.map { mapBlob(it) }

fun mapBlob(blobDTO: BlobDTO): Media {
    val mediaType = when (blobDTO.blobtype.trim().lowercase()) {
        "billede" -> MediaType.IMAGE
        "brosjyre", "bruksanvisning", "quickguide", "målskjema", "batterioversikt" -> MediaType.PDF
        "video" -> MediaType.VIDEO
        else -> {
            println("UNKNOWN ${blobDTO.blobtype} ${blobDTO.blobfile}")
            MediaType.OTHER
        }
    }
    return Media(type = mediaType, text=blobDTO.blobtype.trim(),  uri = blobDTO.blobfile.trim())
}

fun mapDescription(produkt: HmDbProductDTO): Description =
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

