package no.nav.hm.grunndata.db.index.item

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.nulls.shouldNotBeNull
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.index.news.NewsDoc
import no.nav.hm.grunndata.rapid.dto.NewsStatus
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

@MicronautTest
class IndexItemRepositoryTest(private val objectMapper: ObjectMapper,
                              private val indexItemRepository: IndexItemRepository) {

    @Test
    fun crudTest() {
        val news = NewsDoc(
            // default values for NewsDoc
            id = UUID.randomUUID().toString(),
            title = "Test Title",
            created = LocalDateTime.now(),
            updated = LocalDateTime.now(),
            published = LocalDateTime.now(),
            identifier = "testIdentifier",
            text = "testText",
            status = NewsStatus.ACTIVE,
            expired = LocalDateTime.now(),
            createdBy = "createdBy",
            updatedBy = "updatedBy",
            author = "author"
        )

            val indexItem = IndexItem(
            id = UUID.randomUUID(),
            oid = news.id,
            payload = objectMapper.writer().writeValueAsString(news),
            indexType = IndexType.NEWS,
            created = LocalDateTime.now(),
            updated = LocalDateTime.now()
        )

        runBlocking {
            val saved = indexItemRepository.save(indexItem)
            saved.shouldNotBeNull()
            indexItemRepository.findByOid(news.id).shouldNotBeNull()
        }
    }
}