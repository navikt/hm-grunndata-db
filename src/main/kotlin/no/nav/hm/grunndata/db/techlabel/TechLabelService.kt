package no.nav.hm.grunndata.db.techlabel

import io.micronaut.cache.annotation.Cacheable
import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Singleton
open class TechLabelService(
    private val techLabelClient: TechLabelApiClient
)  {

    companion object {
        private val LOG = LoggerFactory.getLogger(TechLabelService::class.java)
    }

  fun fetchLabelsByIsoCode(isocode: String): List<TechLabelDTO> {
        val levels = isocode.length / 2
        val techLabels: MutableList<TechLabelDTO> = mutableListOf()
        for (i in levels downTo 0) {
            val iso = isocode.substring(0, i * 2)
            techLabels.addAll(fetchAllLabelsGroupByIso()[iso] ?: emptyList())
        }
        return techLabels.distinctBy { it.id }
    }

    fun fetchLabelByIsoCodeLabel(isocode: String, label: String): TechLabelDTO? {
        val labelsByIso =  fetchLabelsByIsoCode(isocode)
        labelsByIso.forEach {
            if (it.label == label) return it
        }
        return null
    }

    @Cacheable("techlabels-all-labels")
    open fun fetchAllLabelsGroupByIso(): Map<String, List<TechLabelDTO>> = runBlocking {
        LOG.info("fetching all labels")
        techLabelClient.fetchAllTechLabel()
    }

}

data class TechLabelDTO(
    val id: UUID,
    val label: String,
    val isocode: String,
    val type: String,
    val unit: String?=null,
    val sort: Int,
    val section: String?=null,
    val updated: LocalDateTime
)