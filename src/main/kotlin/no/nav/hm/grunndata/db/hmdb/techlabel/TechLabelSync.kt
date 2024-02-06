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
        LOG.info("Got labels from HMDB ${labels.size}")
        if (labels.size > 1000) {
            val deleted = techLabelRepository.deleteAll()
            LOG.info("Cleaning up ${deleted} labels")
            labels.forEach { label ->
                val saved = techLabelRepository.save(label)
                LOG.info("Saved techlabel ${saved.id} - ${saved.label} - ${saved.identifier}")
            }
        }
    }
}


