package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import no.nav.hm.grunndata.db.product.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.persistence.Table

@MappedEntity
@Table(name="hmdbproduktbatch_v1")
data class HmDbProduktBatch (
    @field:GeneratedValue
    @field:Id
    var id: Long =-1L,
    @field:TypeDef(type = DataType.JSON)
    val produkter: List<ProduktDTO>,
    @field:TypeDef(type = DataType.JSON)
    val tekniskeData: List<TekniskeDataDTO>,
    val created: LocalDateTime = LocalDateTime.now()
)

data class HmDbProduktBatchDTO(
    val produkter: List<ProduktDTO>,
    val tekniskeData: List<TekniskeDataDTO>,
)

fun HmDbProduktBatchDTO.toEntity(): HmDbProduktBatch = HmDbProduktBatch(
    produkter = produkter,
    tekniskeData = tekniskeData
)

fun HmDbProduktBatch.toProductList():List<Product> {
    val byArtId = tekniskeData.groupBy { it.artid }
    return produkter.map { produkt ->
        Product(
            title = produkt.prodname,
            seriesId = produkt.prodid,
            HMDBArtId = produkt.artid,
            HMSArtNr = produkt.stockid,
            isoCategory = produkt.isocode,
            agreement = mapAgreement(produkt),
            data = byArtId.getOrDefault(produkt.artid, emptySet())
                .map {
                    TechData(
                        key = it.techlabeldk!!,
                        value = it.datavalue!!,
                        unit = it.techdataunit!!,
                    )
                }.toSet(),
            supplierId = produkt.supplier!!.toLong(),
            supplierRef = produkt.artno ?: produkt.artid,
            description = mapDescription(produkt),
            media = mapMedia(produkt),
            created = parseDate(produkt.aindate)
        )
    }
}

val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun parseDate(aindate: String?): LocalDateTime {
    try {
        val dateStr = aindate!!.substring(0,aindate!!.lastIndexOf("."))
        return LocalDateTime.parse(dateStr, dateTimeFormatter)
    }
    catch (e: DateTimeParseException) {
         println("Could not parse date $aindate")
    }
    return LocalDateTime.now()
}


fun mapMedia(produkt: ProduktDTO): List<Media> {
    return if (produkt.blobfileURL_snet!=null && produkt.blobtype!=null) {
        val mediaTytpe = when (produkt.blobtype.trim().lowercase()) {
            "billede" -> MediaType.IMAGE
            else -> {
                println(produkt.blobtype)
                MediaType.OTHER
            }
        }
        listOf(Media(uri = produkt.blobfileURL_snet, type = mediaTytpe))
    }
    else emptyList()
}

fun mapDescription(produkt: ProduktDTO): Description =
    Description(modelName = produkt.artname,
        modelDescription = produkt.adescshort,
        text = produkt.pshortdesc)

fun mapAgreement(produkt: ProduktDTO): Agreement? {
    return if (produkt.newsid!=null && null != produkt.newspublish)
        Agreement(agreementId = produkt.newsid,
            agreementStart = produkt.newspublish,
            agreementEnd = produkt.newsexpire!!,
            agreementPostId = produkt.apostid!!,
            agreementPostNr = produkt.apostnr!!,
            agreementPostTitle = produkt.aposttitle!!,
            agreementRank = produkt.postrank!!) else null
}

