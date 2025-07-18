package no.nav.hm.grunndata.db.index.product

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import org.slf4j.LoggerFactory
import java.util.UUID

@Controller("/internal/index/products")
@ExecuteOn(TaskExecutors.BLOCKING)
class ProductIndexerController(private val productIndexer: ProductIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexerController::class.java)
    }

    @Post("/")
    fun indexProducts(@QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        productIndexer.reIndex(alias)
    }

    @Post("/supplier/{supplierId}")
    fun indexProductsBySupplierId(supplierId: UUID) {
        productIndexer.reIndexBySupplierId(supplierId)
    }

    @Post("/series/{seriesId}")
    fun indexProductsBySeriesId(seriesId: UUID) {
        productIndexer.reIndexBySeriesId(seriesId)
    }

    @Post("/isoCategory/{isoCategory}")
    fun indexProductsByCategory(isoCategory: String) {
        productIndexer.reIndexByIsoCategory(isoCategory)
    }

    @Put("/alias/{indexName}")
    fun aliasProducts(indexName: String) {
        productIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    fun getAlias() = productIndexer.getAlias().toJsonString()

    @Delete("{uuid}")
    fun deleteProductById(uuid: UUID) {
        productIndexer.delete(uuid)
    }

    @Get("/count")
    fun search() = productIndexer.count()


}