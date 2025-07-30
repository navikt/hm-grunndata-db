package no.nav.hm.grunndata.db.index.item

import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.index.agreement.AgreementDoc
import no.nav.hm.grunndata.db.index.agreement.AgreementIndexer
import no.nav.hm.grunndata.db.index.external_product.ExternalProductIndexer
import no.nav.hm.grunndata.db.index.news.NewsDoc
import no.nav.hm.grunndata.db.index.news.NewsIndexer
import no.nav.hm.grunndata.db.index.product.ProductIndexer
import no.nav.hm.grunndata.db.index.supplier.SupplierIndexer

data class IndexConfig(
    val aliasIndexName: String,
    val mappings: String,
    val settings: String,
    val indexType: IndexType,
    val searchDocClassType: Class<out SearchDoc>,
)


