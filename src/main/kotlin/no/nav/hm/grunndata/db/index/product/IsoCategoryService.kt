package no.nav.hm.grunndata.db.index.product

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO

@Singleton
class IsoCategoryService(gdbApiClient: GdbApiClient) {

    private val isoCategories: Map<String, IsoCategoryDTO> =
        gdbApiClient.retrieveIsoCategories().associateBy { it.isoCode }

    companion object {
        private val LOG = LoggerFactory.getLogger(IsoCategoryService::class.java)
    }

    init {
        LOG.info("Got isoCategories: ${isoCategories.size}")
    }

    fun lookUpCode(isoCode: String): IsoCategoryDTO? = isoCategories[isoCode]

    fun retrieveAllCategories(): List<IsoCategoryDTO> = isoCategories.values.toList()

    fun getHigherLevelsInBranch(isoCode: String): List<IsoCategoryDTO> {
        val cat = isoCategories[isoCode]
        if (cat==null) LOG.warn("IsoCode: $isoCode not found!")
        return isoCategories.values.filter { isoCode.startsWith(it.isoCode) }
    }

    fun getClosestLevelInBranch(isoCode: String): IsoCategoryDTO? {
        isoCategories.values.sortedByDescending { it.isoLevel }.forEach {
            if (isoCode.startsWith(it.isoCode)) {
                LOG.info("matched $isoCode with: ${it.isoCode} ${it.isoTitle} ${it.isoLevel}")
                return it
            }
        }
        return null
    }

}


