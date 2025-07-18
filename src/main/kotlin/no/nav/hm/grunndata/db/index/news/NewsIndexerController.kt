package no.nav.hm.grunndata.db.index.news

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import org.slf4j.LoggerFactory

@Controller("/internal/index/news")
class NewsIndexerController(private val newsIndexer: NewsIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(NewsIndexerController::class.java)
    }

    @Post("/")
    suspend fun indexNews(@QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        newsIndexer.reIndex(alias)
    }

    @Put("/alias/{indexName}")
    suspend fun aliasNews(indexName: String) {
        newsIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    suspend fun getAlias() = newsIndexer.getAlias().toJsonString()

    @Get("/count")
    suspend fun count() = newsIndexer.docCount()
}