package no.nav.hm.grunndata.db.hmdb.iso

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.hmdb.HmDbClient
import no.nav.hm.grunndata.db.iso.IsoCategory
import no.nav.hm.grunndata.db.iso.IsoCategoryRepository
import no.nav.hm.grunndata.db.iso.IsoTranslations
import org.slf4j.LoggerFactory

@Singleton
class IsoSync(private val hmDbClient: HmDbClient,
              private val isoCategoryRepository: IsoCategoryRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(IsoSync::class.java)
    }

//    suspend fun syncIso() {
//        val isos = hmDbClient.fetchIso().filter { it.isotext != null }
//        val categories = isos.map { it.toIsoCategory() }
//        LOG.info("Got ${categories.size} for updating")
//        categories.forEach {
//            isoCategoryRepository.findById(it.isoCode)?.let {
//                inDb -> isoCategoryRepository.update(it.copy(created = inDb.created))
//            } ?: isoCategoryRepository.save(it)
//        }
//    }

    suspend fun syncIsoWithSearchWords() {
        val isoSearchWords = hmDbClient.fetchIsoSearchwords()
        val isos = isoSearchWords.isos.filter { it.isotext != null }
        val categories = isos.map { it.toIsoCategory(isoSearchWords) }
        categories.forEach { isoCategoryRepository.findByIsoCode(it.isoCode)
            ?.let { inDb -> isoCategoryRepository.update(it.copy(id = inDb.id, created = inDb.created)) } ?: isoCategoryRepository.save(it) }
    }

    private fun IsoDTO.toIsoCategory(isoSearchWord: IsoSearchWord): IsoCategory = IsoCategory(
        isoCode = isocode!!,
        isoTitle = isotitle!!,
        isActive = isactive!!,
        showTech = showtech!!,
        allowMulti = allowmulti!!,
        isoLevel = isolevel!!,
        isoText = isotext!!,
        isoTextShort = isotextshort,
        isoTranslations = IsoTranslations (
            titleEn = engisotitle,
            textEn = engisotext,
        ),
        searchWords = isoSearchWord.searchWords[isocode]?.map { it.searchword.trim() }?: emptyList()
    )
}
