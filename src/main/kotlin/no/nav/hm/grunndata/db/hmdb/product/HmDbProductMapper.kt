package no.nav.hm.grunndata.db.hmdb.product

import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.agreement.AgreementPostRepository
import no.nav.hm.grunndata.db.agreement.AgreementRepository
import no.nav.hm.grunndata.db.hmdb.HmDbIdentifier
import no.nav.hm.grunndata.db.product.*
import no.nav.hm.grunndata.db.product.AttributeNames.*
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import java.time.LocalDateTime

@Singleton
class HmDBProductMapper(private val supplierRepository: SupplierRepository,
                        private val agreementRepository: AgreementRepository,
                        private val agreementPostRepository: AgreementPostRepository) {

    suspend fun mapProduct(prod: HmDbProductDTO, batch: HmDbProductBatchDTO): Product =
        Product(
            supplierId =  supplierRepository.findByIdentifier(prod.supplier!!.HmDbIdentifier())!!.id,
            title = prod.prodname,
            attributes = mapAttributes(prod),
            status = mapStatus(prod),
            HMSArtNr = prod.stockid,
            identifier = "${prod.artid}".HmDbIdentifier(),
            supplierRef = if (prod.artno!=null && prod.artno.isNotBlank()) prod.artno else prod.artid.toString().HmDbIdentifier(),
            isoCategory = prod.isocode,
            seriesId = "${prod.prodid}".HmDbIdentifier(),
            techData = mapTechData(batch.techdata[prod.artid] ?: emptyList()),
            media =  mapBlobs(batch.blobs[prod.prodid] ?: emptyList()),
            agreementInfo = if(prod.newsid!=null) mapAgreementInfo(prod) else null,
            created = prod.aindate,
            updated = prod.achange,
            expired = prod.aoutdate ?: LocalDateTime.now().plusYears(20),
            createdBy = HMDB,
            updatedBy = HMDB
        )

    private suspend fun mapAgreementInfo(prod: HmDbProductDTO): AgreementInfo {
        val agreement = agreementRepository.findByIdentifier("${prod.newsid}".HmDbIdentifier())
        val post = agreementPostRepository.findByIdentifier("${prod.apostid}".HmDbIdentifier())
        if (post!!.agreementId!=agreement!!.id) {
            throw RuntimeException("Wrong agreement state!, should never happen")
        }
        return AgreementInfo(
            id = agreement.id,
            identifier = agreement.identifier,
            rank = prod.postrank,
            postId = post.id,
            postNr = post.nr,
            postIdentifier = post.identifier,
            reference = agreement.reference,
            expired = agreement.expired
        )
    }

    private fun mapTechData(datas: List<TechDataDTO>): List<TechData> = datas.map {
        TechData(key = it.techlabeldk!!, value = it.datavalue!!, unit = it.techdataunit!!)
    }

    private fun mapStatus(prod: HmDbProductDTO): ProductStatus =
        if (prod.aisapproved && prod.isactive) ProductStatus.ACTIVE else
            ProductStatus.INACTIVE

    fun mapBlobs(blobs: List<BlobDTO>): List<Media> =
        blobs.associateBy { it.blobfile }
            .values
            .map { mapBlob(it) }
            .sortedBy { "${it.type}-${it.uri}" }
            .mapIndexed { index, media -> media.copy(order = index + 1) }


    fun mapBlob(blobDTO: BlobDTO): Media {
        val mediaType = when (blobDTO.blobtype.trim().lowercase()) {
            "billede" -> MediaType.IMAGE
            "brosjyre", "produktbl", "bruksanvisning", "brugsanvisning", "quickguide", "målskjema", "batterioversikt" -> MediaType.PDF
            "video" -> MediaType.VIDEO
            else -> {
                if (blobDTO.blobfile.endsWith("pdf", true)) MediaType.PDF
                else MediaType.OTHER
            }
        }
        return Media(type = mediaType, text = blobDTO.blobtype.trim(), uri = blobDTO.blobfile.trim())
    }

    fun mapAttributes(produkt: HmDbProductDTO): Map<String, Any> = mapOf(
        Pair(articlename.name, listOf(produkt.artname)),
        Pair(shortdescription.name, listOfNotNull(produkt.adescshort)),
        Pair(text.name, listOf(produkt.pshortdesc))
    )
}

