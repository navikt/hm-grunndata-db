package no.nav.hm.grunndata.db.hmdb

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.hmdb.iso.IsoDTO
import no.nav.hm.grunndata.db.iso.IsoCategory
import no.nav.hm.grunndata.db.iso.IsoCategoryRepository
import no.nav.hm.grunndata.db.iso.IsoTranslations
import org.slf4j.LoggerFactory

@Singleton
class IsoSync(private val hmDbClient: HmDbClient,
              private val isoCategoryRepository: IsoCategoryRepository,
              private val objectMapper: ObjectMapper) {

    companion object {
        private val LOG = LoggerFactory.getLogger(IsoSync::class.java)
    }

    suspend fun syncIso() {
        val isos = hmDbClient.fetchIso()
        val categories = isos.map { it.toIsoCategory() }
        LOG.info("Got ${categories.size} for updating")
        categories.forEach {
            isoCategoryRepository.findById(it.isoCode)?.let {
                inDb -> isoCategoryRepository.update(it.copy(created = inDb.created))
            } ?: isoCategoryRepository.save(it)
        }
    }

    private fun IsoDTO.toIsoCategory(): IsoCategory = IsoCategory(
        isoCode = isocode!!,
        isoTitle = isotitle!!,
        isActive = isactive!!,
        showTech = showtech!!,
        allowMulti = allowmulti!!,
        isoLevel = isolevel!!,
        isoText = isotext!!,
        isoTranslations = IsoTranslations (
            titleEn = engisotitle,
            textEn = engisotext
        )
    )
}
