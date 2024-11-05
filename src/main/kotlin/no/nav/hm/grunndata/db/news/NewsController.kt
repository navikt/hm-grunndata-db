package no.nav.hm.grunndata.db.news

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.annotation.RequestBean
import java.time.LocalDateTime
import no.nav.hm.grunndata.rapid.dto.NewsDTO
import no.nav.hm.grunndata.rapid.dto.NewsStatus

@Controller("/api/v1/news")
class NewsController(private val newsService: NewsService) {

    @Get("/")
    suspend fun findNews(@RequestBean criteria: NewsCriteria, pageable: Pageable
    ): Page<NewsDTO> = newsService.findNews(buildCriteriaSpec(criteria), pageable)

    private fun buildCriteriaSpec(crit: NewsCriteria): PredicateSpecification<News>? =
        if (crit.isNotEmpty()) {
            where {
                crit.status?.let { root[News::status] eq it }
                crit.updated?.let { root[News::updated] greaterThanOrEqualTo it }
            }
        } else null

}

@Introspected
data class NewsCriteria(
    val status: NewsStatus?,
    val updated: LocalDateTime?
) {
    fun isNotEmpty(): Boolean = status != null || updated != null
}