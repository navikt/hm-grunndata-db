package no.nav.hm.grunndata.db.hmdb.product

import no.nav.hm.grunndata.db.product.*

fun mapBlobs(blobs: List<BlobDTO>): List<Media> =
    blobs.toSet()
        .map { mapBlob(it) }
        .sortedBy { "${it.type}-${it.uri}" }
        .mapIndexed { index, media -> media.copy(order = index+1)}


fun mapBlob(blobDTO: BlobDTO): Media {
    val mediaType = when (blobDTO.blobtype.trim().lowercase()) {
        "billede" -> MediaType.IMAGE
        "brosjyre", "produktbl", "bruksanvisning", "brugsanvisning", "quickguide", "målskjema", "batterioversikt" -> MediaType.PDF
        "video" -> MediaType.VIDEO
        else -> {
            println("UNKNOWN ${blobDTO.blobtype} ${blobDTO.blobfile}")
            MediaType.OTHER
        }
    }
    return Media(type = mediaType, text=blobDTO.blobtype.trim(),  uri = blobDTO.blobfile.trim())
}

fun mapDescription(produkt: HmDbProductDTO): Description =
    Description(name = produkt.artname,
        shortDescription = produkt.adescshort,
        text = produkt.pshortdesc)


