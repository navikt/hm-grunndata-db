package no.nav.hm.grunndata.db.iso

import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Singleton
class IsoCategory22Service(private val registerClient: RegisterClient) {

    private var isoCategories: Map<String, IsoCategory22DTO>

    companion object {
        private val LOG = LoggerFactory.getLogger(IsoCategory22Service::class.java)
    }

    init {
        runBlocking {
            isoCategories = registerClient.getAllIsoCategoriesV22().associateBy { it.isoCode }
            LOG.info("Iso categories initialized with size: ${isoCategories.size}")
        }
    }

    fun lookUpCode(isoCode: String): IsoCategory22DTO? {
        val cat = isoCategories[isoCode]
        if (cat==null) LOG.error("IsoCode: $isoCode not found!")
        return cat
    }

    fun getHigherLevelsInBranch(isoCode: String): List<IsoCategory22DTO> {
        val cat = isoCategories[isoCode]
        if (cat==null) LOG.error("IsoCode: $isoCode not found!")
        return isoCategories.values.filter { isoCode.startsWith(it.isoCode) }
    }

    fun retrieveAllCategories(): List<IsoCategory22DTO> = isoCategories.values.toList()

    fun getClosestLevelInBranch(isoCode: String): IsoCategory22DTO? {
        isoCategories.values.sortedByDescending { it.isoLevel }.forEach {
            if (isoCode.startsWith(it.isoCode)) {
                LOG.info("matched $isoCode with: ${it.isoCode} ${it.isoTitle} ${it.isoLevel}")
                return it
            }
        }
        return null
    }
}
