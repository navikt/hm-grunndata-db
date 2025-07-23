package no.nav.hm.grunndata.db.index.news

import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.IndexName
import no.nav.hm.grunndata.db.index.Indexer
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.news.NewsService
import org.opensearch.client.opensearch.OpenSearchClient
import org.slf4j.LoggerFactory

@Singleton
class NewsIndexer(@Value("\${news.aliasName}") private val aliasName: String,
                  private val newsService: NewsService,
                  private val client: OpenSearchClient): Indexer(client, settings, mapping, aliasName) {

    companion object {
        private val LOG = LoggerFactory.getLogger(NewsIndexer::class.java)
        private val settings = NewsIndexer::class.java
            .getResource("/opensearch/news_settings.json")!!.readText()
        private val mapping = NewsIndexer::class.java
            .getResource("/opensearch/news_mapping.json")!!.readText()
    }


    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(IndexName.news)
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName, settings, mapping)
        }
        val page = newsService.findNews(buildCriteriaSpec = null, Pageable.from(0, 5000, Sort.of(Sort.Order.asc("updated"))))
        val news = page.content.map { it.toDoc() }
        LOG.info("indexing ${news.size} news to $indexName")
        index(news, indexName)
        if (alias) {
            updateAlias(indexName)
        }

    }

}