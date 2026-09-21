package no.nav.hm.grunndata.db.iso

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.Cacheable
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking

@Singleton
@CacheConfig(cacheNames = ["isomap"])
open class IsoMapService(private val registerClient: RegisterClient) {

    @Cacheable("isomap-all")
    open fun retrieveAll(): List<IsoMapDTO> = runBlocking { registerClient.getIsoMapV22() }

}