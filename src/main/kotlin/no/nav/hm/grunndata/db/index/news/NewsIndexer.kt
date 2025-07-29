package no.nav.hm.grunndata.db.index.news

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.indexSettingsMap
import no.nav.hm.grunndata.db.news.NewsService
import org.slf4j.LoggerFactory

@Singleton
class NewsIndexer(private val newsService: NewsService,
                  private val indexer: OpensearchIndexer) {

    companion object {
        private val LOG = LoggerFactory.getLogger(NewsIndexer::class.java)
        val settings = NewsIndexer::class.java
            .getResource("/opensearch/news_settings.json")!!.readText()
        val mapping = NewsIndexer::class.java
            .getResource("/opensearch/news_mapping.json")!!.readText()
    }

    val aliasIndexName = indexSettingsMap[IndexType.NEWS]!!.aliasIndexName

    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(aliasIndexName)
        if (!indexer.indexExists(indexName)) {
            LOG.info("creating index $indexName")
            indexer.createIndex(indexName, settings, mapping)
        }
        val page = newsService.findNews(buildCriteriaSpec = null, Pageable.from(0, 5000, Sort.of(Sort.Order.asc("updated"))))
        val news = page.content.map { IndexDoc(id = it.id.toString(), indexType = IndexType.NEWS, doc = it.toDoc(), indexName = indexName) }
        LOG.info("indexing ${news.size} news to $indexName")
        indexer.indexDoc(news)
        if (alias) {
            indexer.updateAlias(aliasIndexName, indexName)
        }
    }

    fun updateAlias(indexName: String) = indexer.updateAlias(indexName, indexName)
    fun getAlias() = indexer.getAlias(aliasIndexName)
    fun docCount() = indexer.docCount(aliasIndexName)

}