package no.nav.hm.grunndata.db.index.supplier

import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.IndexType
import no.nav.hm.grunndata.db.index.Indexer
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.supplier.SupplierService
import org.opensearch.client.opensearch.OpenSearchClient

import org.slf4j.LoggerFactory

@Singleton
class SupplierIndexer(private val supplierService: SupplierService,
                      @Value("\${suppliers.aliasName}") private val aliasName: String,
                      private val client: OpenSearchClient
): Indexer(client, settings, mapping, aliasName) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexer::class.java)
        private val settings = SupplierIndexer::class.java
            .getResource("/opensearch/suppliers_settings.json")!!.readText()
        private val mapping = SupplierIndexer::class.java
            .getResource("/opensearch/suppliers_mapping.json")!!.readText()
    }


    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(IndexType.suppliers)
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName, settings, mapping)
        }
        val page = supplierService.findSuppliers( null,
            Pageable.from(0,5000, Sort.of(Sort.Order.asc("updated")
        )))
        val suppliers = page.content.map { it.toDoc() }
        LOG.info("indexing ${suppliers.size} suppliers to $indexName")
        index(suppliers, indexName)
        if (alias) {
           updateAlias(indexName)
        }
    }

}
