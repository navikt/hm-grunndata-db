package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.rapid.dto.PakrevdGodkjenningskurs
import no.nav.hm.grunndata.rapid.dto.Produkttype
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.UUID

@Singleton
class DigihotSortiment(
    @Value("\${digihotSortiment.bestillingsordning}")
    private val bestillingsordningUrl : String,
    @Value("\${digihotSortiment.digitalSoknad}")
    private val digitalSoknadUrl : String,
    @Value("\${digihotSortiment.pakrevdGodkjenningskurs}")
    private val pakrevdGodkjenningskursUrl: String,
    @Value("\${digihotSortiment.produkttype}")
    private val produkttypeUrl: String,
    @Value("\${digihotSortiment.isoMetadata}")
    private val isoMetadataUrl: String,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AttributeTagService::class.java)
    }

    private val bestillingsordningMap: Map<String, BestillingsordningDTO> =
        objectMapper.readValue(URI(bestillingsordningUrl).toURL(), object : TypeReference<List<BestillingsordningDTO>>(){}).associateBy { it.hmsnr }

    private val digitalSoknadMap: List<SortimentDTO> =
        objectMapper.readTree(URI(digitalSoknadUrl).toURL()).let { node ->
            require(node.isObject) { "unexpected non-object reply from digihot-sortiment" }
            val res = mutableListOf<SortimentDTO>()
            node.fields().forEachRemaining { (key, value) ->
                require(value.isArray) { "unexpected non-array reply from digihot-sortiment" }
                res.add(
                    SortimentDTO(
                        sortimentKategori = key!!,
                        postIds = value!!.mapNotNull { it.at("/postId").textValue() }.map { UUID.fromString(it) },
                    )
                )
            }
            res
        }

    private val pakrevdGodkjenningskursMap: Map<String, PakrevdGodkjenningskurs> =
        objectMapper.readValue(URI(pakrevdGodkjenningskursUrl).toURL(), object : TypeReference<List<PakrevdGodkjenningskurs>>(){}).associateBy { it.isokode }

    private val produkttypeMap: Map<String, ProdukttypeDTO> =
        objectMapper.readValue(URI(produkttypeUrl).toURL(), object : TypeReference<List<ProdukttypeDTO>>(){}).associateBy { it.isokode }

    private val isoMetadataMap: Map<String, IsoMetadataDTO> =
        objectMapper.readValue(URI(isoMetadataUrl).toURL(), object : TypeReference<List<IsoMetadataDTO>>(){}).associateBy { it.isokode }

    fun isBestillingsordning(hmsnr: String): Boolean = bestillingsordningMap.containsKey(hmsnr)

    fun getBestillingsorning(hmsnr: String): BestillingsordningDTO? = bestillingsordningMap[hmsnr]

    fun getPostIdInDigitalCatalog(postId: UUID): Boolean {
        return digitalSoknadMap.any { it.postIds.contains(postId) }
    }

    fun getSortimentKategoriByPostIdInDigitalCatalog(postId: UUID): String? {
        return digitalSoknadMap.find { it.postIds.contains(postId) }?.sortimentKategori
    }

    fun getPakrevdGodkjenningskurs(isocode: String): PakrevdGodkjenningskurs? {
        val relevantIsoCodePrefix = isocode.take(6)
        return pakrevdGodkjenningskursMap[relevantIsoCodePrefix]
    }

    fun getProdukttype(isocode: String): Produkttype? {
        val relevantIsoCodePrefix = isocode.take(6)
        return produkttypeMap[relevantIsoCodePrefix]?.produkttype?.let { Produkttype.valueOf(it) }
    }

    fun getIsoMetadata(isocode: String): IsoMetadataDTO? {
        val relevantIsoCodePrefix = isocode.take(8)
        return isoMetadataMap[relevantIsoCodePrefix]
    }
}

data class BestillingsordningDTO(
    val hmsnr: String,
    val navn: String,
)

data class SortimentDTO(
    val sortimentKategori: String,
    val postIds: List<UUID>,
)

data class ProdukttypeDTO (
    val isokode: String,
    val produkttype: String,
)

data class IsoMetadataDTO (
    val isokode: String,
    val isotittel: String,
    val kortnavn: String,
)
