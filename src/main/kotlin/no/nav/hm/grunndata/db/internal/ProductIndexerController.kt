package no.nav.hm.grunndata.db.internal

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Put
import kotlinx.coroutines.flow.*
import no.nav.hm.grunndata.db.product.ProductRepository
import no.nav.hm.grunndata.db.search.ProductIndexer
import no.nav.hm.grunndata.db.search.toDoc
import org.slf4j.LoggerFactory


@Controller("/internal/product")
class ProductIndexerController(private val indexer: ProductIndexer, private val repository: ProductRepository) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexerController::class.java)
    }

    @Put("/index")
    suspend fun indexProducts() {
        LOG.info("Indexing all products")
        repository.findAll()
            .onEach { indexer.index(it.toDoc()) }
            .catch { e -> LOG.error("Got exception while indexint ${e.message}") }
            .collect()
    }
}
