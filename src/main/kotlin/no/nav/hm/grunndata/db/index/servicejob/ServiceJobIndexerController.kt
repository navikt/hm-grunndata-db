package no.nav.hm.grunndata.db.index.servicejob

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import org.slf4j.LoggerFactory

@Controller("/internal/index/servicejob")
class ServiceJobIndexerController(private val serviceJobIndexer: ServiceJobIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ServiceJobIndexerController::class.java)
    }

    @Post("/")
    suspend fun indexServiceJobs(@QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        serviceJobIndexer.reIndex(alias)
    }

    @Put("/alias/{indexName}")
    suspend fun aliasServiceJobs(indexName: String) {
        serviceJobIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    suspend fun getAlias() = serviceJobIndexer.getAlias().toJsonString()

    @Get("/count")
    suspend fun count() = serviceJobIndexer.docCount()
}
