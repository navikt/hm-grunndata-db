package no.nav.hm.grunndata.db.iso

import io.micronaut.cache.annotation.Cacheable
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking

@Singleton
open class IsoMapService(private val registerClient: RegisterClient) {

    @Cacheable(cacheNames = ["isomap"])
    open fun retrieveMappingsFromRegister(): Map<String, IsoMapDTO> = runBlocking {
        val isomapList = registerClient.getIsoMapV22()
        LOG.info("Retrieved ${isomapList.size} iso mapping")
        isomapList.associateBy { it.code16 }
    }

    fun mapIso16To22(iso16: String): IsoMapDTO? = retrieveMappingsFromRegister()[iso16]

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(IsoMapService::class.java)
    }
}