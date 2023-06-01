package no.nav.hm.grunndata.db.hmdb.product

import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.hmdb.HmDbIdentifier
import no.nav.hm.grunndata.db.hmdbMediaUrl
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.db.product.Product
import no.nav.hm.grunndata.db.product.ProductAgreement
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.*

import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

@Singleton
class HmDBProductMapper(private val supplierService: SupplierService,
                        private val agreementService: AgreementService,
                        private val isoCategoryService: IsoCategoryService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(HmDBProductMapper::class.java)
    }

    fun mapProduct(prod: HmDbProductDTO, batch: HmDbProductBatchDTO): Product =
        Product(
            id = UUID.randomUUID(),
            supplierId = supplierService.findByIdentifier(prod.supplier!!.HmDbIdentifier())!!.id,
            title = prod.prodname,
            articleName = prod.artname,
            attributes = mapAttributes(prod),
            status = mapStatus(prod),
            hmsArtNr = prod.stockid,
            identifier = "${prod.artid}".HmDbIdentifier(),
            supplierRef = if (!prod.artno.isNullOrBlank()) prod.artno else prod.artid.toString().HmDbIdentifier(),
            isoCategory = isoCategoryService.lookUpCode(prod.isocode)?.isoCode ?: prod.isocode,
            seriesId = "${prod.prodid}".HmDbIdentifier(),
            techData = mapTechData(batch.techdata[prod.artid] ?: emptyList()),
            media = mapBlobs(batch.blobs[prod.prodid] ?: emptyList()),
            agreementId = if (prod.newsid != null) agreementService.findByIdentifier("${prod.newsid}".HmDbIdentifier())?.id else null,
            agreementInfo = if (prod.newsid != null) mapAgreementInfo(prod) else null,
            agreements = mapAgreements(batch.articlePosts[prod.artid] ?: emptyList()),
            created = prod.aindate,
            updated = prod.achange,
            expired = prod.aoutdate ?: LocalDateTime.now().plusYears(20),
            createdBy = HMDB,
            updatedBy = HMDB
        )

    private fun mapAgreements(posts: List<ArticlePostDTO>): List<ProductAgreement> = posts.map { apost ->
        val agreement = agreementService.findByIdentifier("${apost.newsid}".HmDbIdentifier())
        val post = agreement!!.posts.find { it.identifier == "${apost.apostid}".HmDbIdentifier() }
            ?: throw RuntimeException("Wrong agreement state!, should never happen")
        ProductAgreement(
            id = agreement.id,
            identifier = agreement.identifier,
            reference = agreement.reference,
            rank = apost.postrank,
            postNr = post.nr,
            postIdentifier = post.identifier
        )
    }


    private fun mapAgreementInfo(prod: HmDbProductDTO): AgreementInfo {
        val agreement = agreementService.findByIdentifier("${prod.newsid}".HmDbIdentifier())
        val post = agreement!!.posts.find { it.identifier == "${prod.apostid}".HmDbIdentifier() }
            ?: throw RuntimeException("Wrong agreement state!, should never happen")
        return AgreementInfo(
            id = agreement.id,
            identifier = agreement.identifier,
            rank = prod.postrank,
            postNr = post.nr,
            postIdentifier = post.identifier,
            postTitle = post.title,
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
            .map { mapBlob(it) }
            .filter { it.type != MediaType.OTHER && it.type != MediaType.VIDEO }
            .sortedBy { "${it.type}-${it.uri}" }
            .mapIndexed { index, media -> media.copy(priority = index + 1) }


    fun mapBlob(blobDTO: BlobDTO): MediaInfo {
        val blobFile = blobDTO.blobfile.trim()
        val blobType = blobDTO.blobtype.trim().lowercase()
        val mediaType = when (blobType) {
            "billede" -> MediaType.IMAGE
            "brosjyre", "produktbl", "bruksanvisning", "brugsanvisning", "quickguide", "målskjema", "batterioversikt", "seil" -> MediaType.PDF
            "video" -> MediaType.VIDEO
            else -> {
                LOG.error("Unrecognized media type with file: ${blobDTO.blobfile} and type: ${blobDTO.blobtype}")
                MediaType.OTHER
            }
        }

        val typePath = if (mediaType == MediaType.IMAGE) "orig" else {
            when (blobType) {
                "bruksanvisning", "brugsanvisning" -> "brugsvejl"
                "brosjyre", "produktbl" -> "produktblade"
                "quickguide" -> "seriedok/8606"
                "målskjema" -> "seriedok/1468"
                "seil" -> "seriedok/8680"
                "batterioversikt" -> "seriedok/8694"
                else -> "unknown"
            }
        }

        return MediaInfo(
            type = mediaType, text = blobType, sourceUri = "$hmdbMediaUrl/$typePath/$blobFile",
            uri = "$typePath/${blobFile}", source = MediaSourceType.HMDB
        )
    }

    private fun mapAttributes(produkt: HmDbProductDTO): Attributes = Attributes(
        shortdescription = produkt.adescshort ?: "",
        text = produkt.pshortdesc,
        series = produkt.prodname
    )
}

