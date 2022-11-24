package no.nav.hm.grunndata.db.indexer

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import kotlinx.coroutines.flow.*
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import org.slf4j.LoggerFactory


@Controller("/internal/index")
class SupplierIndexerController(private val indexer: SupplierIndexer,
                                private val supplierRepository: SupplierRepository
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexerController::class.java)
    }

    @Put("/supplier")
    suspend fun indexAgreements() {
        LOG.info("Indexing all supplier")
        supplierRepository.findAll()
            .onEach {
                indexer.index(it.toDoc())
            }
            .catch { e -> LOG.error("Got exception while indexing ${e.message}") }
            .collect()
    }

    @Post("/supplier/{indexName}")
    suspend fun indexAgreements(indexName: String) {
        LOG.info("creating index $indexName")
        val success = indexer.createIndex(indexName)
        if (success) {
            LOG.info("index to $indexName")
            supplierRepository.findAll()
                .onEach {
                    indexer.index(it.toDoc(), indexName)
                }
                .catch { e -> LOG.error("Got exception while indexint ${e.message}") }
                .collect()
        }
    }

    @Put("/supplier/alias/{indexName}")
    fun indexAliasTo(indexName:String) {
        LOG.info("Changing alias to $indexName")
        indexer.updateAlias(indexName)
    }

}
