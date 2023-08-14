package no.nav.hm.grunndata.db.hmdb.techlabel

import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.hmdb.HmDbClient
import no.nav.hm.grunndata.db.techlabel.TechLabelRepository
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Singleton
class TechLabelSync(private val hmDbClient: HmDbClient,
                    private val techLabelRepository: TechLabelRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TechLabelSync::class.java)
    }

    suspend fun syncAllTechLabels() {
        val labels = hmDbClient.fetchAllTechlabels().map { it.toTechLabel() }
        labels.forEach { label ->
            val saved = techLabelRepository.findByIdentifier(label.identifier)?.let { inDb ->
                techLabelRepository.update(label.copy(id = inDb.id, updated = LocalDateTime.now(),
                    createdBy = inDb.createdBy))
            } ?: techLabelRepository.save(label)
            LOG.info("Saved techlabel ${saved.id} - ${saved.label} ${saved.identifier}")
        }
    }
}


