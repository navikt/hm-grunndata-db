package no.nav.hm.grunndata.db.index.news

import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.IndexableItem

@Singleton
class NewsIndexItem : IndexableItem {

    private val settings = NewsIndexer::class.java
        .getResource("/opensearch/news_settings.json")!!.readText()
    private val mapping = NewsIndexer::class.java
        .getResource("/opensearch/news_mapping.json")!!.readText()

    override fun getAliasIndexName(): String = "news"

    override fun getMappings(): String = mapping

    override fun getSettings(): String = settings

    override fun getIndexType(): IndexType = IndexType.NEWS

    override fun getSearchDocClassType(): Class<out SearchDoc> = NewsDoc::class.java
}