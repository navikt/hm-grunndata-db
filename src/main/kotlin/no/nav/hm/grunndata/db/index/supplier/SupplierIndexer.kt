package no.nav.hm.grunndata.db.index.supplier

import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.IndexName
import no.nav.hm.grunndata.db.index.Indexer
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.index.item.IndexableItem
import no.nav.hm.grunndata.db.supplier.SupplierService
import org.opensearch.client.opensearch.OpenSearchClient

import org.slf4j.LoggerFactory

@Singleton
class SupplierIndexer(private val supplierService: SupplierService,
                      private val indexableItem: SupplierIndexItem,
                      private val indexer: Indexer) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexer::class.java)
    }


    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(indexableItem.getAliasIndexName())
        if (!indexer.indexExists(indexName)) {
            LOG.info("creating index $indexName")
            indexer.createIndex(indexName, indexableItem.getSettings(), indexableItem.getMappings())
        }
        val page = supplierService.findSuppliers( null,
            Pageable.from(0,5000, Sort.of(Sort.Order.asc("updated")
        )))
        val suppliers = page.content.map { it.toDoc() }
        LOG.info("indexing ${suppliers.size} suppliers to $indexName")
        indexer.index(suppliers, indexName)
        if (alias) {
           indexer.updateAlias(indexName)
        }
    }


}
