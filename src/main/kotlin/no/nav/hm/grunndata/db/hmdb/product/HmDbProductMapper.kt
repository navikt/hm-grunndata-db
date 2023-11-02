package no.nav.hm.grunndata.db.hmdb.product

import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.hmdbMediaUrl
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.db.media.Media
import no.nav.hm.grunndata.db.product.Product
import no.nav.hm.grunndata.db.product.ProductAgreement
import no.nav.hm.grunndata.db.series.Series
import no.nav.hm.grunndata.db.series.SeriesService
import no.nav.hm.grunndata.db.series.toRapidDTO
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.*
import no.nav.hm.grunndata.rapid.event.EventName

import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

@Singleton
class HmDBProductMapper(private val supplierService: SupplierService,
                        private val agreementService: AgreementService,
                        private val isoCategoryService: IsoCategoryService,
                        private val seriesService: SeriesService,
                        private val rapidPushService: GdbRapidPushService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(HmDBProductMapper::class.java)
    }

    fun mapProduct(prod: HmDbProductDTO, batch: HmDbProductBatchDTO): Product {
        val supplier = supplierService.findByIdentifier(prod.supplier!!.HmDbIdentifier())
        if (prod.artno.isNullOrBlank()) LOG.error("This product does not have levArtNR ${prod.artid}")
        return Product(
            id = UUID.randomUUID(),
            supplierId = supplier!!.id,
            title = prod.prodname,
            articleName = prod.artname,
            attributes = mapAttributes(prod),
            status = mapStatus(prod),
            hmsArtNr = prod.stockid,
            identifier = "${prod.artid}".HmDbIdentifier(),
            supplierRef = if (!prod.artno.isNullOrBlank()) prod.artno else prod.artid.toString().HmDbIdentifier(),
            isoCategory = isoCategoryService.lookUpCode(prod.isocode)?.isoCode ?: prod.isocode,
            seriesId = mapSeries(prod, supplier),
            seriesIdentifier = "${prod.prodid}".HmDbIdentifier(),
            techData = mapTechData(batch.techdata[prod.artid] ?: emptyList()),
            media = mapBlobs(batch.blobs[prod.prodid] ?: emptyList()),
            agreementInfo = if (prod.newsid != null) mapAgreementInfo(prod) else null,
            agreements = mapAgreements(batch.articlePosts[prod.artid] ?: emptyList()),
            created = prod.aindate,
            updated = prod.achange,
            expired = prod.aoutdate ?: LocalDateTime.now().plusYears(20),
            createdBy = HMDB,
            updatedBy = HMDB
        )
    }

    private fun mapSeriesStatus(prod: HmDbProductDTO): SeriesStatus {
        return if (prod.poutdate!=null && prod.poutdate.isBefore(LocalDateTime.now()))
            SeriesStatus.INACTIVE
        else SeriesStatus.ACTIVE
    }

    private fun mapSeries(prod: HmDbProductDTO, supplier: Supplier): String {
        val hmDbIdentifier = "${prod.prodid}".HmDbIdentifier()
        var updated = true
        val series = seriesService.findByIdentifier(hmDbIdentifier)?.let {
            // if changed title, then we update series
            if (it.title != prod.prodname || (prod.poutdate!= null && it.expired != prod.poutdate)) seriesService.update(it.copy(
                title = prod.prodname, updated = LocalDateTime.now(), updatedBy = HMDB, expired = prod.poutdate ?: it.expired,
                status = mapSeriesStatus(prod)
            )) else {
                updated = false
                it
            }
        } ?: run {
            LOG.info("Saving new series $hmDbIdentifier")
            seriesService.save(Series ( status = mapSeriesStatus(prod),
                supplierId = supplier.id, title = prod.prodname, identifier = hmDbIdentifier, createdBy = HMDB, updatedBy = HMDB,
                expired = prod.poutdate ?: LocalDateTime.now().plusYears(20)
            ))
        }
        if (updated) rapidPushService.pushDTOToKafka(series.toRapidDTO(), EventName.hmdbseriessyncV1)
        return series.id.toString()
    }

    private fun mapAgreements(posts: List<ArticlePostDTO>): Set<ProductAgreement> = posts.map { apost ->
        LOG.info("mapping agreement with ${apost.newsid} identifier and ${apost.apostid} postIdentifier")
        val agreement = agreementService.findByIdentifier("${apost.newsid}".HmDbIdentifier())
        val post = agreement!!.posts.find { it.identifier == "${apost.apostid}".HmDbIdentifier() }
            ?: throw RuntimeException("Wrong agreement state!, should never happen")
        ProductAgreement(
            id = agreement.id,
            title = agreement.title,
            identifier = agreement.identifier,
            reference = agreement.reference,
            rank = apost.postrank,
            postNr = post.nr,
            postIdentifier = post.identifier
        )
    }.toSet()


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
            expired = agreement.expired,
            title = agreement.title
        )
    }

    private fun mapTechData(datas: List<TechDataDTO>): List<TechData> = datas.map {
        TechData(key = it.techlabeldk!!, value = it.datavalue!!, unit = it.techdataunit!!)
    }

    private fun mapStatus(prod: HmDbProductDTO): ProductStatus =
        if (prod.aisapproved && prod.isactive) ProductStatus.ACTIVE else
            ProductStatus.INACTIVE

    fun mapBlobs(blobs: List<BlobDTO>): List<Media> =
        blobs.map { mapBlob(it) }
            .filter { it.type != MediaType.OTHER && it.type != MediaType.VIDEO }
            .sortedBy { "${it.type}-${it.uri}" }
            .mapIndexed { index, media -> media.copy(priority = index + 1) }


    fun mapBlob(blobDTO: BlobDTO): Media {
        val blobFile = blobDTO.blobfile.trim()
        val blobType = blobDTO.blobtype.trim().lowercase()
        val mediaType = when (blobType) {
            "billede" -> MediaType.IMAGE
            "brosjyre", "produktbl", "bruksanvisning", "brugsanvisning", "quickguide", "målskjema", "batterioversikt", "seil" -> MediaType.PDF
            "video" -> MediaType.VIDEO
            else -> {
                LOG.error("Unrecognized media type with file: $blobFile and type: $blobType")
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

        return Media (
            type = mediaType, text = blobType, sourceUri = "$hmdbMediaUrl/$typePath/$blobFile",
            uri = "$typePath/${blobFile}", source = MediaSourceType.HMDB
        )
    }

    private fun mapAttributes(produkt: HmDbProductDTO): Attributes = Attributes(
        shortdescription = produkt.adescshort ?: "",
        text = produkt.pshortdesc,
        series = produkt.prodname,
        tenderId = produkt.anbudid,
        hasTender = produkt.hasanbud
    )
}

