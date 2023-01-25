package no.nav.hm.grunndata.db.agreement

import io.kotest.common.runBlocking
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class AgreementRepositoryTest(private val agreementRepository: AgreementRepository,
                              private val agreementPostRepository: AgreementPostRepository) {

    @Test
    fun crudTest() {
        val agreement = Agreement(identifier = "HMDB-123", title = "Rammeavtale Rullestoler", resume = "En kort beskrivelse", reference="23-10234",
        text = "En lang beskrivelse", published = LocalDateTime.now(), expired = LocalDateTime.now().plusYears(3))
        val post = AgreementPost(agreementId = agreement.id, title = "Post 1", identifier = "HMDB-321", nr = 1, description = "En beskrive av posten")
        runBlocking {
            agreementRepository.save(agreement)
            agreementPostRepository.save(post)
        }
    }
}
