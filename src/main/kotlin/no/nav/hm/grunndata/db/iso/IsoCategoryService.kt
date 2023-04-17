package no.nav.hm.grunndata.db.iso

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class IsoCategoryService(private val isoCategoryRepository: IsoCategoryRepository) {

    private var isoCategories: Map<String, IsoCategoryDTO>

    companion object {
        private val LOG = LoggerFactory.getLogger(IsoCategoryService::class.java)
    }

    init {
        runBlocking {
            isoCategories = isoCategoryRepository.findAll().map { it.toDTO() }.toList().associateBy {
                it.isoCode
            }
            LOG.info("Iso categories initialized with size: ${isoCategories.size}")
        }
    }

    fun lookUpCode(isoCode: String): IsoCategoryDTO? = isoCategories[isoCode]

    fun retrieveAllCategories(): List<IsoCategoryDTO> = isoCategories.values.toList()

}
