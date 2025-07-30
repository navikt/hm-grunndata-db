package no.nav.hm.grunndata.db.news

import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.index.item.IndexItemService
import no.nav.hm.grunndata.db.index.item.IndexSettings
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.news.toDoc
import no.nav.hm.grunndata.rapid.dto.NewsDTO
import java.util.UUID

@Singleton
class NewsService(private val newsRepository: NewsRepository,
                  private val indexItemService: IndexItemService,
                  private val gdbRapidPushService: GdbRapidPushService) {

    suspend fun findByIdentifier(identifier: String): NewsDTO? = newsRepository.findByIdentifier(identifier)?.toDTO()

    suspend fun findById(id:UUID): NewsDTO? = newsRepository.findById(id)?.toDTO()

    suspend fun update(news: NewsDTO): NewsDTO = newsRepository.update(news.toEntity()).toDTO()
    suspend fun save(news: NewsDTO): NewsDTO = newsRepository.save(news.toEntity()).toDTO()

    suspend fun findNews(buildCriteriaSpec: PredicateSpecification<News>?, pageable: Pageable) =
        newsRepository.findAll(buildCriteriaSpec, pageable).map { it.toDTO() }

    suspend fun saveAndPushToKafka(newsDTO: NewsDTO, eventName: String): NewsDTO {
        val news = newsDTO.toEntity()
        val saved = newsRepository.findById(news.id)?.let { inDb ->
            newsRepository.update(news.copy(id = inDb.id, created = inDb.created, createdBy = inDb.createdBy))
        } ?: newsRepository.save(news)
        gdbRapidPushService.pushDTOToKafka(newsDTO, eventName)
        indexItemService.saveIndexItem(newsDTO.toDoc(), IndexType.NEWS)
        return newsDTO
    }


}