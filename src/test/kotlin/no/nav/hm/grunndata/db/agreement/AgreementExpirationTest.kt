package no.nav.hm.grunndata.db.agreement

import io.kotest.common.runBlocking
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.mockk
import no.nav.hm.grunndata.db.product.Product
import no.nav.hm.grunndata.db.product.ProductAgreement
import no.nav.hm.grunndata.db.product.ProductService
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.*
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@MicronautTest
class AgreementExpirationTest(private val agreementExpiration: AgreementExpiration,
                              private val agreementService: AgreementService,
                              private val supplierService: SupplierService,
                              private val productService: ProductService) {


    @MockBean(RapidPushService::class)
    fun mockRapidService(): RapidPushService = mockk(relaxed = true)

    @Test
    fun testAgreementExpiration() {
        val agreementId = UUID.randomUUID()
        val agreementId2 = UUID.randomUUID()
        val agreement = Agreement(id = agreementId, identifier = "HMDB-123", title = "Rammeavtale Rullestoler", resume = "En kort beskrivelse", reference="23-10234",
            text = "En lang beskrivelse 1", published = LocalDateTime.now(), expired = LocalDateTime.now().plusYears(3),
            posts = listOf(AgreementPost(title = "Post 1", identifier = "HMDB-321", nr = 1, description = "En beskrive av posten")))
        val expired = Agreement(id = agreementId2, identifier = "HMDB-124", title = "Rammeavtale Rullestoler", resume = "En kort beskrivelse", reference="23-10235",
            text = "En lang beskrivelse 2", published = LocalDateTime.now(), expired = LocalDateTime.now().minusYears(1),
            posts = listOf(AgreementPost(title = "Post 1", identifier = "HMDB-322", nr = 1, description = "En beskrive av posten")))

        val productAgreement = ProductAgreement(
            id = agreement.id, identifier = agreement.identifier, reference = agreement.reference,
            rank = 1, postNr = 1, postIdentifier = agreement.posts[0].identifier
        )
        val productAgreement2 = ProductAgreement(
            id = expired.id, identifier = expired.identifier, reference = expired.reference,
            rank = 1, postNr = 1, postIdentifier = expired.posts[0].identifier
        )
        val supplier = supplierService.save(
            Supplier(
                name = "supplier 1", identifier = "unik-identifier",
                status = SupplierStatus.ACTIVE, info = SupplierInfo(email = "test@test")
            )
        )

        runBlocking {
            agreementService.save(agreement)
            agreementService.save(expired)
            productService.saveAndPushTokafka(
                Product(
                    supplierId = supplier.id,
                    identifier = "123",
                    title = "Dette er et produkt",
                    articleName = "Produkt 1",
                    supplierRef = "123",
                    isoCategory = "123456",
                    agreements = setOf(productAgreement, productAgreement2),
                    attributes = Attributes(
                        manufacturer = "Samsung", compatibleWidth = CompatibleWith(seriesIds = setOf(UUID.randomUUID()))
                    )
                ), "test-event"
            )
            val expiredList = agreementService.findByStatusAndExpiredBefore(AgreementStatus.ACTIVE)
            expiredList.size shouldBe 1
            agreementExpiration.expiredAgreements()
            productService.findByAgreementId(expired.id) shouldBe  emptyList()
            val product = productService.findByAgreementId(agreementId)[0]
            product.pastAgreements.size shouldBe 1
            product.pastAgreements.elementAt(0).id shouldBe expired.id
        }

    }
}
