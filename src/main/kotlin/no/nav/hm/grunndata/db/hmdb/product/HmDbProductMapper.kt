package no.nav.hm.grunndata.db.hmdb.product

import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.REGISTER
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.hmdbMediaUrl
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.db.product.Product
import no.nav.hm.grunndata.db.product.ProductAgreement
import no.nav.hm.grunndata.db.series.Series
import no.nav.hm.grunndata.db.series.SeriesService
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.AgreementInfo
import no.nav.hm.grunndata.rapid.dto.Attributes
import no.nav.hm.grunndata.rapid.dto.MediaInfo
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.dto.SeriesData
import no.nav.hm.grunndata.rapid.dto.SeriesStatus
import no.nav.hm.grunndata.rapid.dto.TechData
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
        val media = mapBlobs(batch.blobs[prod.prodid] ?: emptyList())
        val seriesUUID = mapSeries(media, prod, supplier!!)
        return Product(
            id = UUID.randomUUID(),
            supplierId = supplier.id,
            title = prod.prodname,
            published = prod.aindate,
            articleName = prod.artname,
            attributes = mapAttributes(prod),
            status = mapStatus(prod),
            hmsArtNr = prod.stockid,
            identifier = "${prod.artid}".HmDbIdentifier(),
            supplierRef = if (!prod.artno.isNullOrBlank()) prod.artno else prod.artid.toString().HmDbIdentifier(),
            isoCategory = isoCategoryService.lookUpCode(prod.isocode)?.isoCode ?: prod.isocode,
            seriesUUID = seriesUUID,
            seriesId = seriesUUID.toString(),
            seriesIdentifier = "${prod.prodid}".HmDbIdentifier(),
            techData = mapTechData(batch.techdata[prod.artid] ?: emptyList()),
            media = media,
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

    private fun mapSeries(media: Set<MediaInfo>, prod: HmDbProductDTO, supplier: Supplier): UUID {
        val hmDbIdentifier = "${prod.prodid}".HmDbIdentifier()
        val series = seriesService.findByIdentifier(hmDbIdentifier)?.let {inDb->
            if (inDb.updatedBy == REGISTER) {
                LOG.info("Skipping updating series for ${hmDbIdentifier} id: ${inDb.id} because updated from register")
                return inDb.id
            }
            // if changed title, then we update series
            if (inDb.title != prod.prodname || inDb.text!= prod.pshortdesc
                || inDb.isoCategory != prod.isocode || (prod.poutdate!= null && inDb.expired != prod.poutdate)) {
                seriesService.update(inDb.copy(title = prod.prodname, text = prod.pshortdesc,
                    isoCategory = prod.isocode, seriesData = SeriesData(media = media),
                    updated = LocalDateTime.now(), updatedBy = HMDB, expired = prod.poutdate ?: inDb.expired,
                    status = mapSeriesStatus(prod)))
            }
            else {
                inDb
            }
        } ?: run {
            LOG.info("Saving new series $hmDbIdentifier")
            seriesService.save(Series ( status = mapSeriesStatus(prod),
                supplierId = supplier.id, title = prod.prodname, text = prod.pshortdesc, isoCategory = prod.isocode,
                seriesData = SeriesData(media = media),
                identifier = hmDbIdentifier, createdBy = HMDB, updatedBy = HMDB,
                expired = prod.poutdate ?: LocalDateTime.now().plusYears(20)
            ))
        }
        return series.id
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
            postIdentifier = post.identifier,
            postId = post.id,
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
            postId = post.id,
            refNr = post.refNr,
            postTitle = post.title,
            reference = agreement.reference,
            expired = agreement.expired,
            title = agreement.title
        )
    }

    private fun mapTechData(datas: List<TechDataDTO>): List<TechData> = datas.map {
        TechData(key = it.techlabeldk!!, value = it.datavalue!!, unit = it.techdataunit!!)
    }

    private fun mapStatus(prod: HmDbProductDTO): ProductStatus {
        val expired = prod.aoutdate?.isBefore(LocalDateTime.now()) ?: false
        return if (prod.aisapproved && prod.isactive && !expired) ProductStatus.ACTIVE else
            ProductStatus.INACTIVE
    }

    fun mapBlobs(blobs: List<BlobDTO>): Set<MediaInfo> =
        blobs.map { mapBlob(it) }
            .filter { it.type != MediaType.OTHER }
            .sortedBy { "${it.type}-${it.uri}" }
            .mapIndexed { index, media -> media.copy(priority = index + 1) }.toSet()


    fun mapBlob(blobDTO: BlobDTO): MediaInfo {
        val blobFile = blobDTO.blobfile.trim()
        val blobType = blobDTO.blobtype.trim().lowercase()
        val blobUse = blobDTO.blobuse.trim()
        val mediaType = when (blobType) {
            "billede" -> MediaType.IMAGE
            "brosjyre","produktbl", "bruksanvisning","understell", "monteringsanvis",
            "produktsammenli","tekniske tegnin","opplæringsmater","feilkodeoversik",
            "prøvelisens","lisensinformasj","brugsanvisning", "quickguide",
            "målskjema", "batterioversikt", "seil" -> MediaType.PDF
            "video" -> MediaType.VIDEO
            else -> {
                LOG.error("Unrecognized media type with file: $blobFile and type: $blobType")
                MediaType.OTHER
            }
        }

        val typePath = when (mediaType) {
            MediaType.IMAGE -> "orig"
            MediaType.VIDEO -> "video"
            else -> {
                when (blobType) {
                    "bruksanvisning", "brugsanvisning" -> "brugsvejl"
                    "brosjyre", "produktbl" -> "produktblade"
                    else -> "seriedok/$blobUse"
                }
            }
        }

        return MediaInfo (
            type = mediaType, text = blobType, filename = "$typePath/${blobFile}",
            sourceUri = "$hmdbMediaUrl/$typePath/$blobFile", uri = "$typePath/${blobFile}",
            source = MediaSourceType.HMDB, updated = blobDTO.statusdate?: LocalDateTime.now().minusYears(5)
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

