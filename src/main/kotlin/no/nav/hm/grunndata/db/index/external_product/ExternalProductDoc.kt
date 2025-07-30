package no.nav.hm.grunndata.db.index.external_product

import no.nav.hm.grunndata.rapid.dto.*
import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.index.agreement.AgreementLabels
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import java.time.LocalDateTime
import java.util.*

data class ExternalProductDoc(
    override val id: String,
    val supplier: ExternalProductSupplier,
    val title: String,
    val articleName: String,
    val attributes: ExternalAttributesDoc,
    val status: ProductStatus,
    val hmsArtNr: String? = null,
    val identifier: String,
    val supplierRef: String,
    val isoCategory: String,
    val isoCategoryTitle: String?,
    val isoCategoryTitleShort: String?,
    val isoCategoryText: String?,
    val isoCategoryTextShort: String?,
    // FILTERED: val isoSearchTag: List<String>?,
    // FILTERED: val accessory: Boolean = false,
    // FILTERED: val sparePart: Boolean = false,
    val seriesId: String? = null,
    val data: List<TechData> = emptyList(),
    val media: List<ExternalMediaDoc> = emptyList(),
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val expired: LocalDateTime,
    // FILTERED: val createdBy: String,
    // FILTERED: val updatedBy: String,
    // FILTERED: val filters: ExternalTechDataFilters,
    val agreements: List<ExternalAgreementInfoDoc> = emptyList(),
    val hasAgreement: Boolean = false,
) : SearchDoc {
        override fun isDelete(): Boolean = status == ProductStatus.DELETED
    }


data class ExternalAgreementInfoDoc(
    val id: UUID,
    val identifier: String? = null,
    val title: String? = null,
    val label: String,
    val rank: Int,
    val postNr: Int,
    val postIdentifier: String? = null,
    val postTitle: String? = null,
    val postId: UUID? = null,
    // FILTERED: val refNr: String? = null,
    // FILTERED: val reference: String,
    val expired: LocalDateTime,
)

data class ExternalAttributesDoc(
    // FILTERED: val manufacturer: String? = null,
    // FILTERED: val compatibleWith: CompatibleWith? = null,
    // FILTERED: val keywords: List<String>? = null,
    val series: String? = null,
    val shortdescription: String? = null,
    val text: String? = null,
    val url: String? = null,
    // FILTERED: val bestillingsordning: Boolean? = null,
    // FILTERED: val digitalSoknad: Boolean? = null,
    // FILTERED: val sortimentKategori: String? = null,
    // FILTERED: val pakrevdGodkjenningskurs: PakrevdGodkjenningskurs? = null,
    // FILTERED: val produkttype: Produkttype? = null,
    // FILTERED: val tenderId: String? = null,
    // FILTERED: val hasTender: Boolean? = null
)

data class ExternalMediaDoc(
    val uri: String,
    val priority: Int = 1,
    val type: MediaType = MediaType.IMAGE,
    val text: String? = null,
    val source: MediaSourceType = MediaSourceType.HMDB
)

data class ExternalProductSupplier(val id: String, val identifier: String, val name: String)

fun ProductRapidDTO.toExternalDoc(isoCategoryService: IsoCategoryService): ExternalProductDoc = try {
    val onlyActiveAgreements =
        agreements.filter { it.published!!.isBefore(LocalDateTime.now())
                && it.expired.isAfter(LocalDateTime.now()) && it.status == ProductAgreementStatus.ACTIVE}
    val iso = isoCategoryService.lookUpCode(isoCategory)
    ExternalProductDoc(id = id.toString(),
        supplier = ExternalProductSupplier(
            id = supplier.id.toString(), identifier = supplier.identifier, name = supplier.name
        ),
        title = title,
        articleName = articleName,
        attributes = attributes.toExternalDoc(),
        status = status,
        hmsArtNr = hmsArtNr,
        identifier = identifier,
        supplierRef = supplierRef,
        isoCategory = isoCategory,
        isoCategoryTitle = iso?.isoTitle,
        isoCategoryTitleShort = iso?.isoTitleShort,
        isoCategoryText = iso?.isoText,
        isoCategoryTextShort = iso?.isoTextShort,
        // FILTERED: isoSearchTag = isoCategoryService.getHigherLevelsInBranch(isoCategory).map { it.searchWords }.flatten(),
        // FILTERED: accessory = accessory,
        // FILTERED: sparePart = sparePart,
        seriesId = seriesUUID?.toString(),
        data = techData,
        media = media.map { it.toExternalDoc() }.sortedBy { it.priority },
        created = created,
        updated = updated,
        expired = expired,
        // FILTERED: createdBy = createdBy,
        // FILTERED: updatedBy = updatedBy,
        agreements = onlyActiveAgreements.map { it.toExternalDoc() },
        hasAgreement = onlyActiveAgreements.isNotEmpty(),
        // FILTERED: filters = mapExternalTechDataFilters(techData))
    )
} catch (e: Exception) {
    println(isoCategory)
    throw e
}

private fun AgreementInfo.toExternalDoc(): ExternalAgreementInfoDoc = ExternalAgreementInfoDoc(
    id = id,
    identifier = identifier,
    title = title,
    label = AgreementLabels.matchTitleToLabel(title ?: "Annet"),
    rank = rank,
    postNr = postNr,
    postIdentifier = postIdentifier,
    postTitle = postTitle,
    postId = postId,
    // FILTERED: refNr = refNr,
    // FILTERED: reference = reference,
    expired = expired
)

private fun Attributes.toExternalDoc(): ExternalAttributesDoc {
    return ExternalAttributesDoc(
        // FILTERED: manufacturer = manufacturer,
        // FILTERED: keywords = keywords,
        series = series,
        shortdescription = shortdescription,
        text = text,
        url = url,
        // FILTERED: bestillingsordning = bestillingsordning,
        // FILTERED: digitalSoknad = digitalSoknad,
        // FILTERED: sortimentKategori = sortimentKategori,
        // FILTERED: pakrevdGodkjenningskurs = pakrevdGodkjenningskurs,
        // FILTERED: produkttype = produkttype,
        // FILTERED: tenderId = tenderId,
        // FILTERED: hasTender = hasTender,
        // FILTERED: compatibleWith = compatibleWidth
    )
}

fun MediaInfo.toExternalDoc(): ExternalMediaDoc = ExternalMediaDoc(
    uri = uri, priority = priority, type = type, text = text, source = source
)

private fun String.decimalToInt(): Int? {
    val decimalPart = substringBeforeLast(".")
    return decimalPart.toIntOrNull()
}
