package no.nav.hm.grunndata.db.news

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import jakarta.inject.Singleton
import no.nav.hm.grunndata.rapid.dto.NewsDTO
import no.nav.hm.grunndata.rapid.dto.NewsStatus
import java.time.LocalDateTime

@Singleton
class NewsService(private val newsRepository: NewsRepository) {

    suspend fun findByIdentifier(identifier: String): NewsDTO? = newsRepository.findByIdentifier(identifier)?.toDTO()
    suspend fun update(news: NewsDTO): NewsDTO = newsRepository.update(news.toEntity()).toDTO()
    suspend fun save(news: NewsDTO): NewsDTO = newsRepository.save(news.toEntity()).toDTO()

    suspend fun findNews(params: Map<String, String>?, pageable: Pageable): Page<NewsDTO> =
        newsRepository.findAll(buildCriteriaSpec(params), pageable).map { it.toDTO() }

    private fun buildCriteriaSpec(params: Map<String, String>?): PredicateSpecification<News>? =
        params?.let {
            where {
                if (params.contains("status")) root[News::status] eq NewsStatus.valueOf(params["status"]!!)
                if (params.contains("updated")) root[News::updated] greaterThanOrEqualTo LocalDateTime.parse(params["updated"])
            }
        }


}