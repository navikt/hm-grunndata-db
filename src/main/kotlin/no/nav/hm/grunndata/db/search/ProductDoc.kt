package no.nav.hm.grunndata.db.search

import no.nav.hm.grunndata.db.product.*
import java.time.LocalDateTime
import java.util.*

data class ProductDoc(
    var id: Long = -1L,
    val uuid: UUID = UUID.randomUUID(),
    val supplierId: Long,
    val title: String,
    val description: Description,
    val status: ProductStatus = ProductStatus.ACTIVE,
    val HMSArtNr: String?=null,
    val HMDBArtId: String?=null,
    val supplierRef: String,
    val isoCategory: String,
    val accessory: Boolean = false,
    val part: Boolean = false,
    val seriesId: String?=null,
    val data: Set<TechData> = emptySet(),
    val media: List<Media> = emptyList(),
    val agreement: Agreement?=null,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val expired: LocalDateTime = updated.plusYears(20),
    val createdBy: String = "hjelpemiddeldatabasen",
    val updatedBy: String = "hjelpemiddeldatabasen",
    val filters: TechDataFilters
)

data class TechDataFilters(val fyllmateriale:String?, val setebreddeMaksCM: Int?, val setebreddeMinCM: Int?,
                           val brukervektMinKG: Int?, val materialeTrekk:String?, val setedybdeMinCM:Int?,
                           val setedybdeMaksCM: Int?, val setehoydeMaksCM:Int?, val setehoydeMinCM: Int?,
                           val totalVektKG: Int?, val lengdeCM: Int?, val breddeCM: Int?, val beregnetBarn: String?,
                           val brukervektMaksKG: Int?)


fun Product.toDoc(): ProductDoc = ProductDoc(
    id, uuid, supplierId, title, description, status, HMSArtNr, HMDBArtId, supplierRef, isoCategory, accessory, part,
    seriesId,data, media, agreement, created, updated, expired, createdBy, updatedBy, filters = mapTechDataFilters(data)
)

fun mapTechDataFilters(data: Set<TechData>): TechDataFilters {
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
            "Setebredde maks" -> setebreddeMaksCM = it.value.decimalStringToInt()
            "Setebredde min" -> setebreddeMinCM = it.value.decimalStringToInt()
            "Brukervekt min" -> brukervektMinKG = it.value.decimalStringToInt()
            "Materiale i trekk" -> materialeTrekk = it.value
            "Setedybde min" -> setedybdeMinCM = it.value.decimalStringToInt()
            "Setedybde maks" -> setedybdeMaksCM = it.value.decimalStringToInt()
            "Setehøyde maks" -> setehoydeMaksCM = it.value.decimalStringToInt()
            "Setehøyde min" -> setehoydeMinCM = it.value.decimalStringToInt()
            "Totalvekt" -> totalVektKG = it.value.decimalStringToInt()
            "Lengde" -> lengdeCM = it.value.decimalStringToInt()
            "Bredde" -> breddeCM = it.value.decimalStringToInt()
            "Beregnet på barn" -> beregnetBarn = it.value
            "Brukervekt maks" -> brukervektMaksKG = it.value.decimalStringToInt()
        }
    }
    return TechDataFilters(fyllmateriale = fyllmateriale, setebreddeMaksCM = setebreddeMaksCM,
        setebreddeMinCM = setebreddeMinCM, brukervektMinKG = brukervektMinKG, materialeTrekk = materialeTrekk,
        setedybdeMinCM = setedybdeMinCM, setedybdeMaksCM = setedybdeMaksCM, setehoydeMaksCM = setehoydeMaksCM,
        setehoydeMinCM = setehoydeMinCM, totalVektKG = totalVektKG, lengdeCM = lengdeCM, breddeCM = breddeCM,
        beregnetBarn = beregnetBarn, brukervektMaksKG = brukervektMaksKG)
}

private fun String.decimalStringToInt(): Int? = substringBeforeLast(".").toInt()
