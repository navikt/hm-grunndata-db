package no.nav.hm.grunndata.db.index.supplier

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.IndexItemSupport
import no.nav.hm.grunndata.db.supplier.SupplierService
import org.opensearch.client.opensearch.OpenSearchClient
import org.slf4j.LoggerFactory

@Singleton
class SupplierIndexer(private val supplierService: SupplierService,
                      private val client: OpenSearchClient) : OpensearchIndexer(client), IndexItemSupport {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexer::class.java)
        private val settings = SupplierIndexer::class.java
            .getResource("/opensearch/suppliers_settings.json")!!.readText()
        private val mapping = SupplierIndexer::class.java
            .getResource("/opensearch/suppliers_mapping.json")!!.readText()
    }


    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(getAliasIndexName())
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName, getSettings(), getMappings())
        }
        val page = supplierService.findSuppliers( null,
            Pageable.from(0,5000, Sort.of(Sort.Order.asc("updated")
        )))
        val suppliers = page.content.map {
            IndexDoc(
                id = it.id,
                indexType = IndexType.SUPPLIER,
                doc = it.toDoc(),
                indexName = indexName
            )
        }
        LOG.info("indexing ${suppliers.size} suppliers to $indexName")
        indexDoc(suppliers)
        if (alias) {
           updateAlias(aliasName=getAliasIndexName(), indexName = indexName)
        }
    }

    override fun getAliasIndexName(): String = "suppliers"

    override fun getMappings(): String = mapping

    override fun getSettings(): String = settings

    override fun getIndexType(): IndexType = IndexType.SUPPLIER

    override fun getSearchDocClassType(): Class<out SearchDoc> = SupplierDoc::class.java
    fun updateAlias(indexName: String) = updateAlias(aliasName = getAliasIndexName(), indexName = indexName)
    fun getAlias() = getAlias(getAliasIndexName())
    fun docCount() = docCount(getAliasIndexName())


}
