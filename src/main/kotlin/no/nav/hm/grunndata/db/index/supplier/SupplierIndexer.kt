package no.nav.hm.grunndata.db.index.supplier

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.indexSettingsMap
import no.nav.hm.grunndata.db.supplier.SupplierService
import org.slf4j.LoggerFactory

@Singleton
class SupplierIndexer(
    private val supplierService: SupplierService,
    private val indexer: OpensearchIndexer
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexer::class.java)
        private val settings = SupplierIndexer::class.java
            .getResource("/opensearch/suppliers_settings.json")!!.readText()
        private val mapping = SupplierIndexer::class.java
            .getResource("/opensearch/suppliers_mapping.json")!!.readText()
    }
    
    val aliasIndexName = indexSettingsMap[IndexType.SUPPLIER]!!.aliasIndexName

    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(aliasIndexName)
        if (!indexer.indexExists(indexName)) {
            LOG.info("creating index $indexName")
            indexer.createIndex(indexName, settings, mapping)
        }
        val page = supplierService.findSuppliers(
            null,
            Pageable.from(
                0, 5000, Sort.of(
                    Sort.Order.asc("updated")
                )
            )
        )
        val suppliers = page.content.map {
            IndexDoc(
                id = it.id.toString(),
                indexType = IndexType.SUPPLIER,
                doc = it.toDoc(),
                indexName = indexName
            )
        }
        LOG.info("indexing ${suppliers.size} suppliers to $indexName")
        indexer.indexDoc(suppliers)
        if (alias) {
            indexer.updateAlias(aliasName = aliasIndexName, indexName = indexName)
        }
    }


    fun updateAlias(indexName: String) = indexer.updateAlias(aliasName = aliasIndexName, indexName = indexName)
    fun getAlias() = indexer.getAlias(aliasIndexName)
    fun docCount() = indexer.docCount(aliasIndexName)


}
