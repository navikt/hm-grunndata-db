package no.nav.hm.grunndata.db.index.external_product

import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import org.slf4j.LoggerFactory

@Controller("/internal/index/external-products")
@ExecuteOn(TaskExecutors.BLOCKING)
class ExternalProductIndexerController(private val externalProductIndexer: ExternalProductIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ExternalProductIndexerController::class.java)
    }

    @Post("/")
    fun indexProducts(@QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        externalProductIndexer.reIndex(alias)
    }

    @Put("/alias/{indexName}")
    fun aliasProducts(indexName: String) {
        externalProductIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    fun getAlias() = externalProductIndexer.getAlias().toJsonString()
}
