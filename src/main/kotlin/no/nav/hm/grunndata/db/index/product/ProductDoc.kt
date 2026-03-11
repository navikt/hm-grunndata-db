package no.nav.hm.grunndata.db.index.product

import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.index.agreement.AgreementLabels
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.rapid.dto.AgreementInfo
import no.nav.hm.grunndata.rapid.dto.AlternativeFor
import no.nav.hm.grunndata.rapid.dto.Attributes
import no.nav.hm.grunndata.rapid.dto.CompatibleWith
import no.nav.hm.grunndata.rapid.dto.DocumentUrl
import no.nav.hm.grunndata.rapid.dto.MediaInfo
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType
import no.nav.hm.grunndata.rapid.dto.PakrevdGodkjenningskurs
import no.nav.hm.grunndata.rapid.dto.ProductAgreementStatus
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.dto.Produkttype
import no.nav.hm.grunndata.rapid.dto.TechData
import no.nav.hm.grunndata.rapid.dto.WorksWith
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.UUID

private val LOG = LoggerFactory.getLogger(ProductDoc::class.java)

data class ProductDoc(
    override val id: String,
    val supplier: ProductSupplier,
    val title: String,
    val articleName: String,
    val attributes: AttributesDoc,
    val status: ProductStatus,
    val hmsArtNr: String? = null,
    val identifier: String,
    val supplierRef: String,
    val isoCategory: String,
    val isoCategoryTitleInternational: String?,
    val isoCategoryTitle: String?,
    val isoCategoryTitleShort: String?,
    val isoCategoryText: String?,
    val isoCategoryTextShort: String?,
    val isoSearchTag: List<String>?,
    val accessory: Boolean = false,
    val sparePart: Boolean = false,
    val main: Boolean = !(accessory || sparePart),
    val seriesId: String? = null,
    val data: List<TechData> = emptyList(),
    val media: List<MediaDoc> = emptyList(),
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val expired: LocalDateTime,
    val createdBy: String,
    val updatedBy: String,
    val filters: TechDataFilters,
    val mainAgreements: List<AgreementInfoDoc> = emptyList(),
    val agreements: List<AgreementInfoDoc> = emptyList(),
    val hasAgreement: Boolean = false,
    val hasPreviousAgreement: Boolean = false
) : SearchDoc {
    override fun isDelete(): Boolean = status == ProductStatus.DELETED
}


data class AgreementInfoDoc(
    val id: UUID,
    @Deprecated("Use id instead")
    val identifier: String? = null,
    val title: String? = null,
    val label: String,
    val rank: Int,
    val postNr: Int,
    @Deprecated("Use postId instead")
    val postIdentifier: String? = null,
    val postTitle: String? = null,
    val postId: UUID? = null,
    val refNr: String? = null,
    val reference: String,
    val published: LocalDateTime,
    val expired: LocalDateTime,
    val mainProduct: Boolean,
    val accessory: Boolean,
    val sparePart: Boolean,
)

data class AttributesDoc(
    val manufacturer: String? = null,
    val compatibleWith: CompatibleWith? = null,
    val keywords: List<String>? = null,
    val series: String? = null,
    val shortdescription: String? = null,
    val text: String? = null,
    val url: String? = null,
    val documentUrls: List<DocumentUrl>? = null,
    val bestillingsordning: Boolean? = null,
    val digitalSoknad: Boolean? = null,
    val sortimentKategori: String? = null,
    val pakrevdGodkjenningskurs: PakrevdGodkjenningskurs? = null,
    val produkttype: Produkttype? = null,
    val tenderId: String? = null,
    val hasTender: Boolean? = null,
    val alternativeFor: AlternativeFor? = null,
    val worksWith: WorksWith? = null,
    val egnetForKommunalTekniker: Boolean? = null,
    val egnetForBrukerpass: Boolean? = null,
)

data class MediaDoc(
    val uri: String,
    val priority: Int = 1,
    val type: MediaType = MediaType.IMAGE,
    val text: String? = null,
    val source: MediaSourceType = MediaSourceType.HMDB
)

data class TechDataFilters(
    val beregnetBarn: String? = null,
    val breddeCM: Int? = null,
    val brukervektMaksKG: Int? = null,
    val brukervektMinKG: Int? = null,
    val dybdeCM: Int? = null,
    val fyllmateriale: String? = null,
    val innendorsBruk: String? = null,
    val lengdeCM: Int? = null,
    val materialeTrekk: String? = null,
    val rammetype: String? = null,
    val setebreddeCM: Int? = null,
    val setebreddeMaksCM: Int? = null,
    val setebreddeMinCM: Int? = null,
    val setedybdeCM: Int? = null,
    val setedybdeMaksCM: Int? = null,
    val setedybdeMinCM: Int? = null,
    val setehoydeCM: Int? = null,
    val setehoydeMaksCM: Int? = null,
    val setehoydeMinCM: Int? = null,
    val terskelhoydeMaksCM: Int? = null,
    val terskelhoydeMinCM: Int? = null,
    val totalVektKG: Float? = null,
    val totalbreddeCM: Int? = null,
    val totallengdeCM: Int? = null,
    val utendorsBruk: String? = null,
    val skrittlengdeMaksCM: Int? = null,
    val skrittlengdeMinCM: Int? = null,
    val madrasslengdeCM: Int? = null,
    val madrasslengdeMaksCM: Int? = null,
    val madrasslengdeMinCM: Int? = null,
    val madrassbreddeCM: Int? = null,
    val madrassbreddeMaksCM: Int? = null,
    val madrassbreddeMinCM: Int? = null,
    val ryggstottebreddeCM: Int? = null,
)

data class ProductSupplier(val id: String, val identifier: String, val name: String)

fun ProductRapidDTO.toDoc(isoCategoryService: IsoCategoryService): ProductDoc = try {
    val (onlyActiveAgreements, previousAgreements) =
        agreements.partition {
            it.published!!.isBefore(LocalDateTime.now())
                    && it.expired.isAfter(LocalDateTime.now()) && it.status == ProductAgreementStatus.ACTIVE
                    && this.status == ProductStatus.ACTIVE
        }
    val mainAgreements = onlyActiveAgreements.filter { it.mainProduct }
    val iso = isoCategoryService.lookUpCode(isoCategory) ?: isoCategoryService.getClosestLevelInBranch(isoCategory)
    val internationalIso = isoCategoryService.lookUpCode(isoCategory.take(6))
    ProductDoc(
        id = id.toString(),
        supplier = ProductSupplier(
            id = supplier.id.toString(), identifier = supplier.identifier, name = supplier.name
        ),
        title = title,
        articleName = articleName,
        attributes = attributes.toDoc(),
        status = status,
        hmsArtNr = hmsArtNr,
        identifier = identifier,
        supplierRef = supplierRef,
        isoCategory = isoCategory,
        isoCategoryTitle = iso?.isoTitle,
        isoCategoryTitleShort = iso?.isoTitleShort,
        isoCategoryText = iso?.isoText,
        isoCategoryTextShort = iso?.isoTextShort,
        isoSearchTag = isoCategoryService.getHigherLevelsInBranch(isoCategory).map { it.searchWords }.flatten(),
        isoCategoryTitleInternational = internationalIso?.isoTitle ?: iso?.isoTitle,
        accessory = accessory,
        sparePart = sparePart,
        main = mainProduct,
        seriesId = seriesUUID?.toString(),
        data = techData,
        media = media.map { it.toDoc() }.sortedBy { it.priority },
        created = created,
        updated = updated,
        expired = expired,
        createdBy = createdBy,
        updatedBy = updatedBy,
        agreements = onlyActiveAgreements.map { it.toDoc() },
        hasAgreement = onlyActiveAgreements.isNotEmpty(),
        mainAgreements = mainAgreements.map { it.toDoc() },
        hasPreviousAgreement = previousAgreements.isNotEmpty(),
        filters = mapTechDataFilters(techData)
    )


} catch (e: Exception) {
    println("Error while mapping id:$id  and iso: $isoCategory")
    throw e
}

fun AgreementInfo.toDoc(): AgreementInfoDoc = AgreementInfoDoc(
    id = id,
    identifier = identifier,
    title = title,
    label = AgreementLabels.matchTitleToLabel(title ?: "Annet"),
    rank = rank,
    postNr = postNr,
    postIdentifier = postIdentifier,
    postTitle = postTitle,
    postId = postId,
    refNr = refNr,
    reference = reference,
    expired = expired,
    published = published ?: LocalDateTime.now(),
    mainProduct = mainProduct,
    accessory = accessory,
    sparePart = sparePart
)

fun Attributes.toDoc(): AttributesDoc {
    return AttributesDoc(
        manufacturer = manufacturer,
        keywords = keywords,
        series = series,
        shortdescription = shortdescription,
        text = text,
        url = url,
        documentUrls = documentUrls,
        bestillingsordning = bestillingsordning,
        digitalSoknad = digitalSoknad,
        sortimentKategori = sortimentKategori,
        pakrevdGodkjenningskurs = pakrevdGodkjenningskurs,
        produkttype = produkttype,
        tenderId = tenderId,
        hasTender = hasTender,
        compatibleWith = compatibleWith,
        alternativeFor = alternativeFor,
        worksWith = worksWith,
        egnetForKommunalTekniker = egnetForKommunalTekniker,
        egnetForBrukerpass = egnetForBrukerpass
    )
}


fun MediaInfo.toDoc(): MediaDoc = MediaDoc(
    uri = uri, priority = priority, type = type, text = text, source = source
)

fun mapTechDataFilters(data: List<TechData>): TechDataFilters {
    try {
        val techDataMap = data.associate { it.key to it.value }

        return TechDataFilters(
            fyllmateriale = techDataMap["Fyllmateriale"]?.ifEmpty { null },
            setebreddeMaksCM = techDataMap["Setebredde maks"]?.decimalToInt(),
            setebreddeMinCM = techDataMap["Setebredde min"]?.decimalToInt(),
            brukervektMinKG = techDataMap["Brukervekt min"]?.decimalToInt(),
            materialeTrekk = techDataMap["Materiale i trekk"]?.ifEmpty { null },
            setedybdeMinCM = techDataMap["Setedybde min"]?.decimalToInt(),
            setedybdeMaksCM = techDataMap["Setedybde maks"]?.decimalToInt(),
            setehoydeMaksCM = techDataMap["Setehøyde maks"]?.decimalToInt(),
            setehoydeMinCM = techDataMap["Setehøyde min"]?.decimalToInt(),
            totalVektKG = techDataMap["Totalvekt"]?.decimalToFloat(),
            lengdeCM = techDataMap["Lengde"]?.decimalToInt(),
            breddeCM = techDataMap["Bredde"]?.decimalToInt(),
            dybdeCM = techDataMap["Dybde"]?.decimalToInt(),
            beregnetBarn = techDataMap["Beregnet på barn"]?.ifEmpty { null },
            brukervektMaksKG = techDataMap["Brukervekt maks"]?.decimalToInt(),
            setebreddeCM = techDataMap["Setebredde"]?.decimalToInt(),
            setedybdeCM = techDataMap["Setedybde"]?.decimalToInt(),
            setehoydeCM = techDataMap["Setehøyde"]?.decimalToInt(),
            innendorsBruk = techDataMap["Innendørs bruk"]?.ifEmpty { null },
            utendorsBruk = techDataMap["Utendørs bruk"]?.ifEmpty { null },
            rammetype = techDataMap["Rammetype"]?.ifEmpty { null },
            totalbreddeCM = techDataMap["Totalbredde"]?.decimalToInt(),
            totallengdeCM = techDataMap["Totallengde"]?.decimalToInt(),
            terskelhoydeMaksCM = techDataMap["Terkselhøyde maks"]?.decimalToInt(),
            terskelhoydeMinCM = techDataMap["Terkselhøyde min"]?.decimalToInt(),
            skrittlengdeMaksCM = techDataMap["Skrittlengde maks"]?.decimalToInt(),
            skrittlengdeMinCM = techDataMap["Skrittlengde min"]?.decimalToInt(),
            madrasslengdeMaksCM = techDataMap["Madrass lengde maks"]?.decimalToInt(),
            madrasslengdeCM = techDataMap["Madrass lengde"]?.decimalToInt(),
            madrasslengdeMinCM = techDataMap["Madrass lengde min"]?.decimalToInt(),
            madrassbreddeCM = techDataMap["Madrass bredde"]?.decimalToInt(),
            madrassbreddeMaksCM = techDataMap["Madrass bredde maks"]?.decimalToInt(),
            madrassbreddeMinCM = techDataMap["Madrass bredde min"]?.decimalToInt(),
            ryggstottebreddeCM = techDataMap["Ryggstøtte bredde"]?.decimalToInt(),
        )
    } catch (e: Exception) {
        LOG.error("Error mapping techdatafilters ${e.message}", e)
        return TechDataFilters()
    }
}

private fun String.decimalToInt(): Int = if (this.isNotEmpty()) normalizeDecimalMark().substringBeforeLast(".").toInt() else 0
private fun String.decimalToFloat(): Float = if (this.isNotEmpty()) normalizeDecimalMark().toFloat() else 0.0F

private fun String.normalizeDecimalMark() = replace(",", ".")
