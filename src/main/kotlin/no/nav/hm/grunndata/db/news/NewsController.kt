package no.nav.hm.grunndata.db.news

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import no.nav.hm.grunndata.rapid.dto.NewsDTO

@Controller("/api/v1/news")
class NewsController(private val newsService: NewsService) {

    @Get("/{?params*}")
    suspend fun findNews(@QueryValue params: Map<String, String>?, pageable: Pageable
    ): Page<NewsDTO> = newsService.findNews(params, pageable)


}