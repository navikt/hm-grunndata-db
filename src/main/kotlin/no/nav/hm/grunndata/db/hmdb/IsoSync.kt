package no.nav.hm.grunndata.db.hmdb

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.hmdb.iso.IsoDTO
import no.nav.hm.grunndata.db.iso.IsoCategory
import org.slf4j.LoggerFactory

@Singleton
class IsoSync(private val hmDbClient: HmDbClient, private val objectMapper: ObjectMapper) {

    companion object {
        private val LOG = LoggerFactory.getLogger(IsoSync::class.java)
    }

    fun syncIso() {
        val isos = hmDbClient.fetchIso()
        val categories = isos.map { it.toIsoCategory() }
        LOG.info("${objectMapper.writeValueAsString(categories)}")
    }

    private fun IsoDTO.toIsoCategory(): IsoCategory = IsoCategory(
        isoCode = isocode!!,
        isoTitle = isotitle!!,
        isActive = isactive!!,
        showTech = showtech!!,
        allowMulti = showtech!!,
        isoLevel = isolevel!!,
        isoText = isotext!!,
        isoTextLong = isotextlong!!,
        isoTextShort = isotextshort!!,
        isoTitleEn = engisotitle!!,
        isoTextShortEn = engisotextshort!!,
        isoTextLongEn = engisotextlong!!
    )

}
