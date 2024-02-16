package no.nav.hm.grunndata.db.news

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.rapid.dto.NewsStatus
import org.junit.jupiter.api.Test

@MicronautTest
class NewsRepositoryTest(private val newsRepository: NewsRepository) {

    @Test
    fun crudTest() {
        val news = News(title = "Test news", text = "This is a test news", author = "tester", identifier = "HMDB-20815")
        runBlocking {
            val saved = newsRepository.save(news)
            val found = newsRepository.findById(saved.id)
            found.shouldNotBeNull()
            found.status shouldBe NewsStatus.ACTIVE
            found.title shouldBe "Test news"
            found.text shouldBe "This is a test news"
            found.author shouldBe "tester"
            found.identifier shouldBe "HMDB-20815"
            val updated = newsRepository.update(
                found.copy(
                    text = "This is a test news updated",
                    status = NewsStatus.INACTIVE,
                    updatedBy = "tester"
                )
            )
            updated.shouldNotBeNull()
            updated.text shouldBe "This is a test news updated"
            updated.status shouldBe NewsStatus.INACTIVE

        }
    }
}