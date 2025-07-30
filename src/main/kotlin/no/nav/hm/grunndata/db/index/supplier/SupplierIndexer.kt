package no.nav.hm.grunndata.db.index.supplier

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.item.IndexSettings
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.supplier.SupplierService
import org.slf4j.LoggerFactory

@Singleton
class SupplierIndexer(
    private val supplierService: SupplierService,
    private val indexSettings: IndexSettings,
    private val indexer: OpensearchIndexer
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexer::class.java)
    }
    
    val aliasIndexName = indexSettings.indexConfigMap[IndexType.SUPPLIER]!!.aliasIndexName

    suspend fun reIndex(alias: Boolean) {
        val indexName = indexSettings.createIndexForReindex(IndexType.SUPPLIER)
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
