package no.nav.hm.grunndata.db.index.agreement

import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.IndexableItem

@Singleton
class AgreementIndexItem: IndexableItem {
    companion object {

        val settings = AgreementIndexer::class.java
        .getResource("/opensearch/agreements_settings.json")!!.readText()
        val mapping = AgreementIndexer::class.java
        .getResource("/opensearch/agreements_mapping.json")!!.readText()
    }

    override fun getAliasIndexName(): String = "agreements"

    override fun getMappings(): String = mapping

    override fun getSettings(): String  = settings

    override fun getIndexType(): IndexType = IndexType.AGREEMENT

    override fun getSearchDocClassType(): Class<out SearchDoc> = AgreementDoc::class.java
}