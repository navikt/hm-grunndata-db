package no.nav.hm.grunndata.db.hmdb

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.agreement.AgreementRepository
import no.nav.hm.grunndata.db.hmdb.agreement.HmDbAgreementDTO
import org.junit.jupiter.api.Test

@MicronautTest
class SyncAgreementTest(private val syncScheduler: SyncScheduler,
                        private val objectMapper: ObjectMapper,
                        private val agreementRepository: AgreementRepository) {

    @MockBean(HmDbClient::class)
    fun mockedHMdbClient(): HmDbClient = mockk()

    @Inject
    lateinit var mockClient: HmDbClient

    @Test
    fun syncAgreement() {
        every {
            mockClient.fetchAgreements()
        } answers {
            objectMapper.readValue(SyncAgreementTest::class.java.classLoader.getResourceAsStream("./agreements/agreements.json"),
                object : TypeReference<List<HmDbAgreementDTO>>() {}
            )
        }
        syncScheduler.syncAgreements()
        runBlocking {
            val agreement = agreementRepository.findByIdentifier("HMDB-6427")
            agreement.shouldNotBeNull()
            agreement.reference shouldBe "17-1920"
            agreement.attachments.size shouldBe 7
            println(objectMapper.writeValueAsString(agreement))
        }
    }

}