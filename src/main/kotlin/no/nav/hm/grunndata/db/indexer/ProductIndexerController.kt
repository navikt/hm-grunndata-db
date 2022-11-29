package no.nav.hm.grunndata.db.indexer

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import kotlinx.coroutines.flow.*
import no.nav.hm.grunndata.db.product.ProductRepository
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import org.slf4j.LoggerFactory


@Controller("/internal/index")
class ProductIndexerController(private val indexer: ProductIndexer,
                               private val repository: ProductRepository,
                               private val supplierRepository: SupplierRepository) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexerController::class.java)
    }

    @Put("/products")
    suspend fun indexProducts() {
        LOG.info("Indexing all products")
            repository.findAll()
                .onEach { indexer.index(it.toDoc(supplier = supplierRepository.findById(it.supplierId)!!)) }
                .catch { e -> LOG.error("Got exception while indexint ${e.message}") }
                .collect()
    }

    @Post("/products/{indexName}")
    suspend fun indexProducts(indexName: String) {
        LOG.info("creating index $indexName")
        val success = indexer.createIndex(indexName)
        val docList = mutableListOf<ProductDoc>()
        if (success) {
            LOG.info("index to $indexName")
            repository.findAll()
                .onEach {
                    if (docList.size==500) {
                        LOG.info("indexing ${docList.size} items")
                        indexer.index(docList, indexName)
                        docList.clear()
                    }
                    docList.add(it.toDoc(supplier = supplierRepository.findById(it.supplierId)!!))
                }
                .catch { e -> LOG.error("Got exception while indexing ${e.message}") }
                .collect()
            LOG.info("indexing last ${docList.size} items")
            indexer.index(docList, indexName)
        }
    }

    @Put("/products/alias/{indexName}")
    fun indexAliasTo(indexName:String) {
        LOG.info("Changing alias to $indexName")
        indexer.updateAlias(indexName)
    }

}
