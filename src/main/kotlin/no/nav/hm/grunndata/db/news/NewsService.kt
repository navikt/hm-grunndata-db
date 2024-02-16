package no.nav.hm.grunndata.db.news

import jakarta.inject.Singleton
import no.nav.hm.grunndata.rapid.dto.NewsDTO

@Singleton
class NewsService(private val newsRepository: NewsRepository) {

    suspend fun findByIdentifier(identifier: String): NewsDTO? = newsRepository.findByIdentifier(identifier)?.toDTO()
    suspend fun update(news: NewsDTO): NewsDTO = newsRepository.update(news.toEntity()).toDTO()
    suspend fun save(news: NewsDTO): NewsDTO = newsRepository.save(news.toEntity()).toDTO()
}