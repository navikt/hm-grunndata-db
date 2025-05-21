package no.nav.hm.grunndata.db.techlabel

import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Singleton
class TechLabelService(
    private val techLabelClient: TechLabelApiClient
)  {

    private var techLabelsByIso: Map<String, List<TechLabelDTO>>

    companion object {
        private val LOG = LoggerFactory.getLogger(TechLabelService::class.java)
    }

    init {
        runBlocking {
            techLabelsByIso = techLabelClient.fetchAllTechLabel()
            LOG.info("Init techLabels: ${techLabelsByIso.values.size}")
        }
    }

  fun fetchLabelsByIsoCode(isocode: String): List<TechLabelDTO> {
        val levels = isocode.length / 2
        val techLabels: MutableList<TechLabelDTO> = mutableListOf()
        for (i in levels downTo 0) {
            val iso = isocode.substring(0, i * 2)
            techLabels.addAll(techLabelsByIso[iso] ?: emptyList())
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

    fun fetchAllLabels(): Map<String, List<TechLabelDTO>> = techLabelsByIso

}

data class TechLabelDTO(
    val id: UUID,
    val identifier: String,
    val label: String,
    val guide: String,
    val definition: String?,
    val isocode: String,
    val type: String,
    val unit: String?,
    val sort: Int,
    val isKeyLabel: Boolean = false,
    val systemLabel: String,
    val options: List<String> = emptyList(),
    val createdBy: String,
    val updatedBy: String,
    val created: LocalDateTime,
    val updated: LocalDateTime
)