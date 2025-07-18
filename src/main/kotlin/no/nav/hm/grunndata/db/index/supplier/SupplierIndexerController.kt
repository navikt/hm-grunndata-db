package no.nav.hm.grunndata.db.index.supplier

import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import org.slf4j.LoggerFactory

@Controller("/internal/index/suppliers")
@ExecuteOn(TaskExecutors.BLOCKING)
class SupplierIndexerController(private val supplierIndexer: SupplierIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexerController::class.java)
    }

    @Post("/")
    suspend fun indexSuppliers(@QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        supplierIndexer.reIndex(alias)
    }

    @Put("/alias/{indexName}")
    suspend fun aliasSuppliers(indexName: String) {
        supplierIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    suspend fun getAlias() = supplierIndexer.getAlias().toJsonString()

    @Get("/count")
    suspend fun count() = supplierIndexer.docCount()
}
