package no.nav.hm.grunndata.db.indexer

import no.nav.hm.grunndata.db.product.*
import no.nav.hm.grunndata.db.supplier.Supplier
import java.time.LocalDateTime

data class ProductDoc (
    override val id: String,
    val supplier: ProductSupplier,
    val title: String,
    val attributes: Map<String,Any> = emptyMap(),
    val status: ProductStatus,
    val HMSArtNr: String?=null,
    val identifier: String,
    val supplierRef: String,
    val isoCategory: String,
    val accessory: Boolean = false,
    val sparePart: Boolean = false,
    val seriesId: String?=null,
    val data: List<TechData> = emptyList(),
    val media: List<Media> = emptyList(),
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val expired: LocalDateTime,
    val createdBy: String,
    val updatedBy: String,
    val filters: TechDataFilters,
    val agreementInfo: AgreementInfo?,
    val hasAgreement: Boolean = false,
): SearchDoc


data class TechDataFilters(val fyllmateriale:String?, val setebreddeMaksCM: Int?, val setebreddeMinCM: Int?,
                           val brukervektMinKG: Int?, val materialeTrekk:String?, val setedybdeMinCM:Int?,
                           val setedybdeMaksCM: Int?, val setehoydeMaksCM:Int?, val setehoydeMinCM: Int?,
                           val totalVektKG: Int?, val lengdeCM: Int?, val breddeCM: Int?, val beregnetBarn: String?,
                           val brukervektMaksKG: Int?)

data class ProductSupplier(val id: String, val identifier: String, val name: String)

fun Product.toDoc(supplier: Supplier): ProductDoc = ProductDoc (
    id = id.toString(), supplier = ProductSupplier(id=supplier.id.toString(), identifier=supplier.identifier, name= supplier.name),
    title = title, attributes = attributes, status = status, HMSArtNr = HMSArtNr, identifier = identifier,
    supplierRef = supplierRef, isoCategory = isoCategory, accessory = accessory, sparePart = sparePart, seriesId = seriesId,
    data = techData, media = media, created = created, updated = updated, expired = expired, createdBy = createdBy,
    updatedBy = updatedBy, agreementInfo = agreementInfo, hasAgreement = agreementInfo!=null,
    filters = mapTechDataFilters(techData)
)



fun mapTechDataFilters(data: List<TechData>): TechDataFilters {
    var fyllmateriale:String? = null
    var setebreddeMaksCM: Int? = null
    var setebreddeMinCM: Int? = null
    var brukervektMinKG: Int? = null
    var materialeTrekk:String? = null
    var setedybdeMinCM:Int? = null
    var setedybdeMaksCM: Int? = null
    var setehoydeMaksCM:Int? = null
    var setehoydeMinCM: Int? = null
    var totalVektKG: Int? = null
    var lengdeCM: Int? = null
    var breddeCM: Int? = null
    var beregnetBarn: String? = null
    var brukervektMaksKG: Int? = null
    data.forEach {
        when (it.key) {
            "Fyllmateriale" -> fyllmateriale = it.value
            "Setebredde maks" -> setebreddeMaksCM = it.value.decimalToInt()
            "Setebredde min" -> setebreddeMinCM = it.value.decimalToInt()
            "Brukervekt min" -> brukervektMinKG = it.value.decimalToInt()
            "Materiale i trekk" -> materialeTrekk = it.value
            "Setedybde min" -> setedybdeMinCM = it.value.decimalToInt()
            "Setedybde maks" -> setedybdeMaksCM = it.value.decimalToInt()
            "Setehøyde maks" -> setehoydeMaksCM = it.value.decimalToInt()
            "Setehøyde min" -> setehoydeMinCM = it.value.decimalToInt()
            "Totalvekt" -> totalVektKG = it.value.decimalToInt()
            "Lengde" -> lengdeCM = it.value.decimalToInt()
            "Bredde" -> breddeCM = it.value.decimalToInt()
            "Beregnet på barn" -> beregnetBarn = it.value
            "Brukervekt maks" -> brukervektMaksKG = it.value.decimalToInt()
        }
    }
    return TechDataFilters(fyllmateriale = fyllmateriale, setebreddeMaksCM = setebreddeMaksCM,
        setebreddeMinCM = setebreddeMinCM, brukervektMinKG = brukervektMinKG, materialeTrekk = materialeTrekk,
        setedybdeMinCM = setedybdeMinCM, setedybdeMaksCM = setedybdeMaksCM, setehoydeMaksCM = setehoydeMaksCM,
        setehoydeMinCM = setehoydeMinCM, totalVektKG = totalVektKG, lengdeCM = lengdeCM, breddeCM = breddeCM,
        beregnetBarn = beregnetBarn, brukervektMaksKG = brukervektMaksKG)
}

private fun String.decimalToInt(): Int? = substringBeforeLast(".").toInt()
