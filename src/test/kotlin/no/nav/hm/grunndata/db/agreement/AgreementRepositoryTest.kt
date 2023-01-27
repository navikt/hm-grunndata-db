package no.nav.hm.grunndata.db.agreement

import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@MicronautTest
class AgreementRepositoryTest(private val agreementRepository: AgreementRepository) {

    @Test
    fun crudTest() {
        val agreementId = UUID.randomUUID()
        val agreement = Agreement(id = agreementId, identifier = "HMDB-123", title = "Rammeavtale Rullestoler", resume = "En kort beskrivelse", reference="23-10234",
        text = "En lang beskrivelse", published = LocalDateTime.now(), expired = LocalDateTime.now().plusYears(3),
            posts = listOf(AgreementPost(title = "Post 1", identifier = "HMDB-321", nr = 1, description = "En beskrive av posten")))
        runBlocking {
            val saved = agreementRepository.save(agreement)
            saved.shouldNotBeNull()
            val read = agreementRepository.findById(saved.id)
            read.shouldNotBeNull()
            val updated = agreementRepository.update(read.copy(title = "Ny title"))
            updated.shouldNotBeNull()
            updated.title shouldBe "Ny title"
            updated.posts.size shouldBe  1
        }
    }
}
