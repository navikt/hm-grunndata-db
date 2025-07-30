package no.nav.hm.grunndata.db.index.news

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.item.IndexSettings
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.news.NewsService
import org.slf4j.LoggerFactory

@Singleton
class NewsIndexer(private val newsService: NewsService,
                  private val indexSettings: IndexSettings,
                  private val indexer: OpensearchIndexer) {

    companion object {
        private val LOG = LoggerFactory.getLogger(NewsIndexer::class.java)
    }

    val aliasIndexName = indexSettings.indexConfigMap[IndexType.NEWS]!!.aliasIndexName

    suspend fun reIndex(alias: Boolean) {
        val indexName = indexSettings.createIndexForReindex(IndexType.NEWS)
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