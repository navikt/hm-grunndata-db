package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.rapid.dto.PakrevdGodkjenningskurs
import no.nav.hm.grunndata.rapid.dto.Produkttype
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URL
import java.nio.charset.Charset

@Singleton
class DigihotSortiment(
    @Value("\${digihotSortiment.bestillingsordning}")
    private val bestillingsordningUrl : String,
    @Value("\${digihotSortiment.digitalSoknad}")
    private val digitalSoknadUrl : String,
    @Value("\${digihotSortiment.ikkeTilInstitusjon}")
    private val ikkeTilInstitusjonUrl : String,
    @Value("\${digihotSortiment.pakrevdGodkjenningskurs}")
    private val pakrevdGodkjenningskursUrl: String,
    @Value("\${digihotSortiment.produkttype}")
    private val produkttypeUrl: String,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AttributeTagService::class.java)
    }

    private val bestillingsordningMap: Map<String, BestillingsordningDTO> =
        objectMapper.readValue(URI(bestillingsordningUrl).toURL(), object : TypeReference<List<BestillingsordningDTO>>(){}).associateBy { it.hmsnr }

    private val digitalSoknadMap: Map<Int, String> =
        objectMapper.readTree(URI(digitalSoknadUrl).toURL()).let { node ->
            require(node.isObject) { "unexpected non-object reply from digihot-sortiment" }
            val res = mutableMapOf<Int, String>()
            node.fields().forEachRemaining { (key, value) ->
                require(value.isArray) { "unexpected non-array reply from digihot-sortiment" }
                value!!.forEach { apostid ->
                    res[apostid.intValue()] = key!!
                }
            }
            res
        }

    private val ikkeTilInstitusjonMap: Map<String, IkkeTilInstitusjonDTO> =
        objectMapper.readValue(URI(ikkeTilInstitusjonUrl).toURL(), object : TypeReference<List<IkkeTilInstitusjonDTO>>(){}).associateBy { it.isokode }

    private val pakrevdGodkjenningskursMap: Map<String, PakrevdGodkjenningskurs> =
        objectMapper.readValue(URI(pakrevdGodkjenningskursUrl).toURL(), object : TypeReference<List<PakrevdGodkjenningskurs>>(){}).associateBy { it.isokode }

    private val produkttypeMap: Map<String, ProdukttypeDTO> =
        objectMapper.readValue(URI(produkttypeUrl).toURL(), object : TypeReference<List<ProdukttypeDTO>>(){}).associateBy { it.isokode }

    fun isBestillingsordning(hmsnr: String): Boolean = bestillingsordningMap.containsKey(hmsnr)

    fun getBestillingsorning(hmsnr: String): BestillingsordningDTO? = bestillingsordningMap[hmsnr]

    fun getApostIdInDigitalCatalog(apostid: Int): Boolean {
        return digitalSoknadMap.containsKey(apostid)
    }

    fun getIkkeTilInstitusjon(isocode: String): Boolean {
        val relevantIsoCodePrefix = isocode.take(6)
        return ikkeTilInstitusjonMap.containsKey(relevantIsoCodePrefix)
    }

    fun getPakrevdGodkjenningskurs(isocode: String): PakrevdGodkjenningskurs? {
        val relevantIsoCodePrefix = isocode.take(6)
        return pakrevdGodkjenningskursMap[relevantIsoCodePrefix]
    }

    fun getProdukttype(isocode: String): Produkttype? {
        val relevantIsoCodePrefix = isocode.take(6)
        return produkttypeMap[relevantIsoCodePrefix]?.produkttype?.let { Produkttype.valueOf(it) }
    }
}

data class BestillingsordningDTO(
    val hmsnr: String,
    val navn: String,
)

data class IkkeTilInstitusjonDTO(
    val isokode: String,
)

data class ProdukttypeDTO (
    val isokode: String,
    val produkttype: String,
)