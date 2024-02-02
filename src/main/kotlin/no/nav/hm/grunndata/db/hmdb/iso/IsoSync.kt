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

    private val additionalIsoSearchWord : Map<String,List<String>> = mapOf(
        "1222" to listOf("MRS"),
        "180915" to listOf("Løftestol, Stol med oppreistfunksjon"),
        "1223" to listOf("ERS"),
        "0912" to listOf("Dusjstol", "Toalettstol", "Dostol"),
        "1236" to listOf("Personheis, Heis"),
        "1810" to listOf("Trykkavlastende pute", "Antidecubituspute", "Pute med trykkavlastende effekt"),
        "1809" to listOf("Arbeidsstol", "Husmorstol"),
        "123103" to listOf("Slidelaken", "Silkelaken", "Sklilaken", "Glidelaken"),
        "120606" to listOf("Gåbord", "Gåstativ", "Gåramme")
    )

    suspend fun syncIsoWithSearchWords() {
        val isoSearchWords = hmDbClient.fetchIsoSearchwords()
        val isos = isoSearchWords.isos.filter { it.isotext != null }
        val categories = isos.map { it.toIsoCategory(isoSearchWords) }
        categories.forEach { isoCategoryRepository.findByIsoCode(it.isoCode)
            ?.let { inDb -> isoCategoryRepository.update(it.copy(id = inDb.id, created = inDb.created)) } ?: isoCategoryRepository.save(it) }
    }

    private fun IsoDTO.toIsoCategory(isoSearchWord: IsoSearchWord): IsoCategory {
        val additionWords = additionalIsoSearchWord[isocode] ?: emptyList()
        val inDbWords = isoSearchWord.searchWords[isocode]?.map { it.searchword.trim() } ?: emptyList()
        val searchWords = (inDbWords + additionWords).distinct()
        return IsoCategory(
            isoCode = isocode!!,
            isoTitle = isotitle!!,
            isActive = isactive!!,
            showTech = showtech!!,
            allowMulti = allowmulti!!,
            isoLevel = isolevel!!,
            isoText = isotext!!,
            isoTextShort = isotextshort,
            isoTranslations = IsoTranslations(
                titleEn = engisotitle,
                textEn = engisotext,
            ),
            searchWords = searchWords
        )
    }
}
