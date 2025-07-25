package no.nav.hm.grunndata.db.index.external_product

import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.IndexableItem

class ExternalIndexItem : IndexableItem {

    private val settings = ExternalProductIndexer::class.java
        .getResource("/opensearch/external_products_settings.json")!!.readText()
    private val mapping = ExternalProductIndexer::class.java
        .getResource("/opensearch/external_products_mapping.json")!!.readText()

    override fun getAliasIndexName(): String = "external_products"

    override fun getMappings(): String = mapping

    override fun getSettings(): String = settings

    override fun getIndexType(): IndexType = IndexType.EXTERNAL_PRODUCT

    override fun getSearchDocClassType(): Class<out SearchDoc>  = ExternalProductDoc::class.java
}