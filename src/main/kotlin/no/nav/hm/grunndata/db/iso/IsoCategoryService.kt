package no.nav.hm.grunndata.db.iso

import jakarta.inject.Singleton
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO
import org.slf4j.LoggerFactory

@Singleton
open class IsoCategoryService(private val registerClient: RegisterClient) {

    private var isoCategories: Map<String, IsoCategoryDTO>

    companion object {
        private val LOG = LoggerFactory.getLogger(IsoCategoryService::class.java)
    }

    init {
        runBlocking {
            isoCategories = registerClient.getAllIsoCategories().associateBy {
                it.isoCode
            }
            LOG.info("Iso categories initialized with size: ${isoCategories.size}")
        }
    }

    fun lookUpCode(isoCode: String): IsoCategoryDTO? {
        val cat = isoCategories[isoCode]
        if (cat==null) LOG.warn("IsoCode: $isoCode not found!")
        return cat
    }

    fun getHigherLevelsInBranch(isoCode: String): List<IsoCategoryDTO> {
        val cat = isoCategories[isoCode]
        if (cat==null) LOG.warn("IsoCode: $isoCode not found!")
        return isoCategories.values.filter { isoCode.startsWith(it.isoCode) }
    }

    fun retrieveAllCategories(): List<IsoCategoryDTO> = isoCategories.values.toList()

}
