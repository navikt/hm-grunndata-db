package no.nav.hm.grunndata.db.index.news

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.IndexItemSupport
import no.nav.hm.grunndata.db.news.NewsService
import org.slf4j.LoggerFactory

@Singleton
class NewsIndexer(private val newsService: NewsService,
                  private val indexer: OpensearchIndexer) :  IndexItemSupport {

    companion object {
        private val LOG = LoggerFactory.getLogger(NewsIndexer::class.java)
        private val settings = NewsIndexer::class.java
            .getResource("/opensearch/news_settings.json")!!.readText()
        private val mapping = NewsIndexer::class.java
            .getResource("/opensearch/news_mapping.json")!!.readText()
    }

    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(getAliasIndexName())
        if (!indexer.indexExists(indexName)) {
            LOG.info("creating index $indexName")
            indexer.createIndex(indexName, getSettings(), getMappings())
        }
        val page = newsService.findNews(buildCriteriaSpec = null, Pageable.from(0, 5000, Sort.of(Sort.Order.asc("updated"))))
        val news = page.content.map { IndexDoc(id = it.id.toString(), indexType = IndexType.NEWS, doc = it.toDoc(), indexName = indexName) }
        LOG.info("indexing ${news.size} news to $indexName")
        indexer.indexDoc(news)
        if (alias) {
            indexer.updateAlias(getAliasIndexName(), indexName)
        }
    }

    fun updateAlias(indexName: String) = indexer.updateAlias(indexName, indexName)
    fun getAlias() = indexer.getAlias(getAliasIndexName())
    fun docCount() = indexer.docCount(getAliasIndexName())

    override fun getAliasIndexName(): String = "news"

    override fun getMappings(): String = mapping

    override fun getSettings(): String = settings

    override fun getIndexType(): IndexType = IndexType.NEWS

    override fun getSearchDocClassType(): Class<out SearchDoc> = NewsDoc::class.java


}