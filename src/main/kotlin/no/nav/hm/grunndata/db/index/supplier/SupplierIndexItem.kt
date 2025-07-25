package no.nav.hm.grunndata.db.index.supplier

import io.micronaut.context.annotation.Context
import io.micronaut.core.annotation.Order
import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.IndexableItem

@Context
@Order(1)
class SupplierIndexItem: IndexableItem {

    companion object {
        private val settings = SupplierIndexer::class.java
            .getResource("/opensearch/suppliers_settings.json")!!.readText()
        private val mapping = SupplierIndexer::class.java
            .getResource("/opensearch/suppliers_mapping.json")!!.readText()
    }

    override fun getAliasIndexName(): String = "suppliers"

    override fun getMappings(): String = mapping

    override fun getSettings(): String = settings

    override fun getIndexType(): IndexType = IndexType.SUPPLIER

    override fun getSearchDocClassType(): Class<out SearchDoc> = SupplierDoc::class.java


}