package no.nav.hm.grunndata.db.hmdb.product

import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.agreement.AgreementRepository
import no.nav.hm.grunndata.db.hmdb.HmDbIdentifier
import no.nav.hm.grunndata.db.hmdbMediaUrl
import no.nav.hm.grunndata.db.product.*
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import no.nav.hm.grunndata.db.supplier.toDTO
import no.nav.hm.grunndata.rapid.dto.*
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.LocalDateTime
import java.util.*

@Singleton
class HmDBProductMapper(private val supplierRepository: SupplierRepository,
                        private val agreementRepository: AgreementRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(HmDBProductMapper::class.java)
    }
    suspend fun mapProduct(prod: HmDbProductDTO, batch: HmDbProductBatchDTO): ProductDTO =
        ProductDTO(
            id = UUID.randomUUID(),
            supplier =  supplierRepository.findByIdentifier(prod.supplier!!.HmDbIdentifier())!!.toDTO(),
            title = prod.prodname,
            articleName = prod.artname,
            attributes = mapAttributes(prod),
            status = mapStatus(prod),
            hmsArtNr = prod.stockid,
            identifier = "${prod.artid}".HmDbIdentifier(),
            supplierRef = if (!prod.artno.isNullOrBlank()) prod.artno else prod.artid.toString().HmDbIdentifier(),
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
        val post = agreement!!.posts.find { it.identifier == "${prod.apostid}".HmDbIdentifier() }
            ?: throw RuntimeException("Wrong agreement state!, should never happen")
        return AgreementInfo(
            id = agreement.id,
            identifier = agreement.identifier,
            rank = prod.postrank,
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

    fun mapBlobs(blobs: List<BlobDTO>): List<MediaInfo> =
        blobs.associateBy { it.blobfile }
            .values
            .map { mapBlob(it)}
            .filter{ it.type != MediaType.OTHER }
            .sortedBy { "${it.type}-${it.uri}" }
            .mapIndexed { index, media -> media.copy(priority = index + 1) }


    fun mapBlob(blobDTO: BlobDTO): MediaInfo {
        val blobFile = blobDTO.blobfile.trim()
        val blobType = blobDTO.blobtype.trim().lowercase()
        val mediaType = when (blobType) {
            "billede" -> MediaType.IMAGE
            "brosjyre", "produktbl", "bruksanvisning", "brugsanvisning", "quickguide", "målskjema", "batterioversikt" -> MediaType.PDF
            else -> {
                if (blobFile.endsWith("pdf", true)) MediaType.PDF
                else {
                    LOG.error("Unrecognized media type with file: ${blobDTO.blobfile} and type: ${blobDTO.blobtype}")
                    MediaType.OTHER
                }
            }
        }
        val typePath = when (blobType) {
            "bruksanvisning", "brugsanvisning" -> "brugsvejl"
            "brosjyre", "produktbl" -> "produktblade"
            else -> "orig"
        }

        return MediaInfo(type = mediaType, text = blobType, sourceUri = "$hmdbMediaUrl/$typePath/$blobFile",
            uri = "${blobFile}", source = MediaSourceType.HMDB)
    }

    fun mapAttributes(produkt: HmDbProductDTO): Map<AttributeNames, Any> = mapOf(
        AttributeNames.shortdescription to (produkt.adescshort ?: ""),
        AttributeNames.text to produkt.pshortdesc,
        AttributeNames.series to listOf(produkt.prodname)
    )
}

