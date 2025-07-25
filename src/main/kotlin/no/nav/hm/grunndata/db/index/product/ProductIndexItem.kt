package no.nav.hm.grunndata.db.index.product

import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.IndexableItem


class ProductIndexItem: IndexableItem {

    companion object {
        private val settings = ProductIndexer::class.java
            .getResource("/opensearch/products_settings.json")!!.readText()
        private val mapping = ProductIndexer::class.java
            .getResource("/opensearch/products_mapping.json")!!.readText()
    }

    override fun getAliasIndexName(): String = "products"

    override fun getMappings(): String = mapping

    override fun getSettings(): String = settings

    override fun getIndexType(): IndexType = IndexType.PRODUCT

    override fun getSearchDocClassType(): Class<out SearchDoc> = ProductDoc::class.java

}