package no.nav.hm.grunndata.db.product

import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.mockk
import no.nav.hm.grunndata.db.agreement.Agreement
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.*
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@MicronautTest
class ProductServiceTest(private val productService: ProductService,
                         private val supplierService: SupplierService,
                         private val agreementService: AgreementService) {

    @MockBean(RapidPushService::class)
    fun mockRapidService(): RapidPushService = mockk(relaxed = true)

    @Test
    fun productServiceTest() {
        val agreementId = UUID.randomUUID()
        val agreement = Agreement(id = agreementId, identifier = "HMDB-123", title = "Rammeavtale Rullestoler", resume = "En kort beskrivelse", reference="23-10234",
            text = "En lang beskrivelse 1", published = LocalDateTime.now(), expired = LocalDateTime.now().plusYears(3),
            posts = listOf(AgreementPost(title = "Post 1", identifier = "HMDB-321", nr = 1, description = "En beskrive av posten")))

        val productAgreement = ProductAgreement(
            id = agreement.id, identifier = agreement.identifier, reference = agreement.reference,
            rank = 1, postNr = 1, postIdentifier = agreement.posts[0].identifier
        )
        val supplier = supplierService.save(
            Supplier(
                name = "supplier 1", identifier = "unik-identifier",
                status = SupplierStatus.ACTIVE, info = SupplierInfo(email = "test@test")
            )
        )
        runBlocking {
            agreementService.save(agreement)
            val product = productService.saveAndPushTokafka(
                Product(
                    supplierId = supplier.id,
                    identifier = "123",
                    title = "Dette er et produkt",
                    articleName = "Produkt 1",
                    supplierRef = "123",
                    isoCategory = "123456",
                    agreementId = agreementId,
                    agreements = listOf(productAgreement, productAgreement),
                    attributes = Attributes(
                        manufacturer = "Samsung", compatible = listOf(CompatibleAttribute(hmsArtNr = "1"))
                    )
                ), "test-event"
            )
            product.shouldNotBeNull()
            val products = productService.findByAgreementId(agreementId)
            products.size shouldBe 1
        }
    }
}
