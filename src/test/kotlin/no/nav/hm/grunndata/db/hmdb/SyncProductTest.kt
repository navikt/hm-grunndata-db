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
import no.nav.hm.grunndata.db.agreement.AgreementRepository
import no.nav.hm.grunndata.db.product.ProductRepository
import org.junit.jupiter.api.Test

@MicronautTest
class SyncProductTest(private val syncScheduler: SyncScheduler,
                      private val objectMapper: ObjectMapper,
                      private val productRepository: ProductRepository) {

//    @MockBean(HmDbClient::class)
//    fun mockedHMdbClient(): HmDbClient = mockk()

//    @Inject
//    lateinit var mockClient: HmDbClient

    @Test
    fun syncProducts() {
//        every {
//            mockClient.fetchAgreements()
//        } answers {
//            objectMapper.readValue(SyncProductTest::class.java.classLoader.getResourceAsStream("./agreements/agreements.json"),
//                object : TypeReference<List<HmDbAgreementDTO>>() {}
//            )
//        }
        syncScheduler.syncSuppliers()
        syncScheduler.syncProducts()
    }

}