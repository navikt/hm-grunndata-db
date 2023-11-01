package no.nav.hm.grunndata.db.agreement

import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.rapid.dto.AgreementPost
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@MicronautTest
class AgreementRepositoryTest(private val agreementRepository: AgreementRepository) {

    @Test
    fun crudTest() {
        val agreementId = UUID.randomUUID()
        val agreementId2 = UUID.randomUUID()
        val agreement = Agreement(id = agreementId, identifier = "HMDB-123", title = "Rammeavtale Rullestoler", resume = "En kort beskrivelse", reference="23-10234",
        text = "En lang beskrivelse 1", published = LocalDateTime.now(), expired = LocalDateTime.now().plusYears(3),
            posts = listOf(AgreementPost(title = "Post 1", identifier = "HMDB-321", nr = 1, description = "En beskrive av posten")), isoCategory = listOf("1", "2"))
        val agreement2 = Agreement(id = agreementId2, identifier = "HMDB-124", title = "Rammeavtale Rullestoler", resume = "En kort beskrivelse", reference="23-10235",
            text = "En lang beskrivelse 2", published = LocalDateTime.now(), expired = LocalDateTime.now().plusYears(3),
            posts = listOf(AgreementPost(title = "Post 1", identifier = "HMDB-322", nr = 1, description = "En beskrive av posten")),
            isoCategory = listOf("1", "2"))
        runBlocking {
            val saved = agreementRepository.save(agreement)
            val saved2 = agreementRepository.save(agreement2)
            saved.shouldNotBeNull()
            saved2.shouldNotBeNull()
            val read = agreementRepository.findByIdentifier("HMDB-123")
            val read2 = agreementRepository.findByIdentifier("HMDB-124")
            read.shouldNotBeNull()
            read2.shouldNotBeNull()
            read.text shouldBe "En lang beskrivelse 1"
            read2.text shouldBe "En lang beskrivelse 2"
            val updated = agreementRepository.update(read.copy(title = "Ny title"))
            updated.shouldNotBeNull()
            updated.title shouldBe "Ny title"
            updated.posts.size shouldBe  1
            updated.isoCategory.size shouldBe 2
            updated.isoCategory[0] shouldBe "1"
            updated.isoCategory[1] shouldBe "2"
        }
    }
}
