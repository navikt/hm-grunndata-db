package no.nav.hm.grunndata.db.index.external_product

import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Controller("/internal/index/external-products")
class ExternalProductIndexerController(private val externalProductIndexer: ExternalProductIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ExternalProductIndexerController::class.java)
    }

    @Post("/")
    suspend fun indexProducts(@QueryValue(value = "alias", defaultValue = "false") alias: Boolean,
                              @QueryValue(value = "from") from: LocalDateTime? = null, @QueryValue(value = "size") size: Int? = null) {
        externalProductIndexer.reIndex(alias, from, size ?: 3000)
    }

    @Put("/alias/{indexName}")
    suspend fun aliasProducts(indexName: String) {
        externalProductIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    suspend fun getAlias() = externalProductIndexer.getAlias().toJsonString()
}
