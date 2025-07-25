package no.nav.hm.grunndata.db.index.news

import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.IndexName
import no.nav.hm.grunndata.db.index.Indexer
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.IndexableItem
import no.nav.hm.grunndata.db.news.NewsService
import org.opensearch.client.opensearch.OpenSearchClient
import org.slf4j.LoggerFactory

@Singleton
class NewsIndexer(private val newsService: NewsService,
                  private val indexableItem: NewsIndexItem,
                  private val indexer: Indexer) {

    companion object {
        private val LOG = LoggerFactory.getLogger(NewsIndexer::class.java)

    }


    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(indexableItem.getAliasIndexName())
        if (!indexer.indexExists(indexName)) {
            LOG.info("creating index $indexName")
            indexer.createIndex(indexName, indexableItem.getSettings(), indexableItem.getMappings())
        }
        val page = newsService.findNews(buildCriteriaSpec = null, Pageable.from(0, 5000, Sort.of(Sort.Order.asc("updated"))))
        val news = page.content.map { IndexDoc(id = it.id, indexType = IndexType.NEWS, doc = it.toDoc(), indexName = indexName) }
        LOG.info("indexing ${news.size} news to $indexName")
        indexer.indexDoc(news)
        if (alias) {
            indexer.updateAlias(indexableItem.getAliasIndexName(), indexName)
        }
    }

    fun updateAlias(indexName: String) {}

}