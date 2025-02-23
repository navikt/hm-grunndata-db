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
import java.util.UUID

@Singleton
class NewsService(private val newsRepository: NewsRepository) {

    suspend fun findByIdentifier(identifier: String): NewsDTO? = newsRepository.findByIdentifier(identifier)?.toDTO()

    suspend fun findById(id:UUID): NewsDTO? = newsRepository.findById(id)?.toDTO()

    suspend fun update(news: NewsDTO): NewsDTO = newsRepository.update(news.toEntity()).toDTO()
    suspend fun save(news: NewsDTO): NewsDTO = newsRepository.save(news.toEntity()).toDTO()

    suspend fun findNews(buildCriteriaSpec: PredicateSpecification<News>?, pageable: Pageable) =
        newsRepository.findAll(buildCriteriaSpec, pageable).map { it.toDTO() }


}