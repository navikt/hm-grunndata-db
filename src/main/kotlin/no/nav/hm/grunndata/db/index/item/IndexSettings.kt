package no.nav.hm.grunndata.db.index.item

import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.index.agreement.AgreementDoc
import no.nav.hm.grunndata.db.index.agreement.AgreementIndexer
import no.nav.hm.grunndata.db.index.external_product.ExternalProductIndexer
import no.nav.hm.grunndata.db.index.news.NewsDoc
import no.nav.hm.grunndata.db.index.news.NewsIndexer
import no.nav.hm.grunndata.db.index.product.ProductIndexer

data class IndexSettings(
    val aliasIndexName: String,
    val mappings: String,
    val settings: String,
    val indexType: IndexType,
    val searchDocClassType: Class<out SearchDoc>,
)

val indexSettingsMap = mutableMapOf<IndexType, IndexSettings>().apply {
    put(
        IndexType.AGREEMENT,
        IndexSettings(
            aliasIndexName = "agreements",
            mappings = AgreementIndexer.mapping,
            settings = AgreementIndexer.settings,
            indexType = IndexType.AGREEMENT,
            searchDocClassType = AgreementDoc::class.java
        )
    )
    put(
        IndexType.NEWS,
        IndexSettings(
            aliasIndexName = "news",
            mappings = NewsIndexer.mapping,
            settings = NewsIndexer.settings,
            indexType = IndexType.NEWS,
            searchDocClassType = NewsDoc::class.java
        )
    )
    put(
        IndexType.PRODUCT,
        IndexSettings(
            aliasIndexName = "products",
            mappings = ProductIndexer.mapping,
            settings = ProductIndexer.settings,
            indexType = IndexType.PRODUCT,
            searchDocClassType = SearchDoc::class.java // Placeholder, should be replaced with actual ProductDoc class
        )
    )
    put(
        IndexType.EXTERNAL_PRODUCT,
        IndexSettings(
            aliasIndexName = "external_products",
            mappings = ExternalProductIndexer.mapping,
            settings = ExternalProductIndexer.settings,
            indexType = IndexType.EXTERNAL_PRODUCT,
            searchDocClassType = SearchDoc::class.java
        )
    )
}
