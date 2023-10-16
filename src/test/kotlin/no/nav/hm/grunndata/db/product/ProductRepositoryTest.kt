package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.mockk
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.*
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class ProductRepositoryTest(private val productRepository: ProductRepository,
                            private val supplierService: SupplierService,
                            private val objectMapper: ObjectMapper) {

    @MockBean(RapidPushService::class)
    fun mockRapidService(): RapidPushService = mockk(relaxed = true)

    @Test
    fun readSavedDb() {
        val seriesId = UUID.randomUUID().toString()
        val agreementId = UUID.randomUUID()
        val agreementId2 = UUID.randomUUID()
        val productAgreement = ProductAgreement(
            id = agreementId, identifier = "HMDB-1", reference = "19-123",
            rank=1, postNr = 5, postIdentifier = "HMDB-4123"
        )

        val productAgreement2 = ProductAgreement(
            id = agreementId2, identifier = "HMDB-2", reference = "19-124",
            rank=1, postNr = 2, postIdentifier = "HMDB-3123"
        )

        runBlocking {
            val supplier = supplierService.save(Supplier(name = "supplier 1", identifier = "unik-identifier",
                status = SupplierStatus.ACTIVE, info = SupplierInfo(email = "test@test")))
            val product = productRepository.save(Product(
                supplierId = supplier.id, identifier = "123", title = "Dette er et produkt", articleName = "Produkt 1",
                supplierRef = "123", isoCategory = "123456", seriesId = seriesId, seriesIdentifier = seriesId,
                agreements = setOf(productAgreement, productAgreement2),
                attributes = Attributes (
                    manufacturer =  "Samsung",  compatibleWidth = CompatibleWith(seriesIds = setOf(UUID.randomUUID())))
            ))
           productRepository.save(Product(
                supplierId = supplier.id, identifier = "124", title = "Dette er et produkt2", articleName = "Produkt 2",
                supplierRef = "124", isoCategory = "123456",
                agreements = setOf(productAgreement),
                attributes = Attributes (
                    manufacturer =  "Samsung",  compatibleWidth = CompatibleWith(seriesIds = setOf(UUID.randomUUID())))
            ))
            val db = productRepository.findById(product.id)
            db.shouldNotBeNull()
            db.supplierId shouldBe product.supplierId
            db.title shouldBe product.title
            db.articleName shouldBe "Produkt 1"
            db.agreements!!.size shouldBe 2
            db.agreements!!.elementAt(0).rank shouldBe 1
            db.seriesIdentifier shouldBe seriesId
            db.seriesId shouldBe  seriesId

            val updated = productRepository.update(db.copy(title = "Dette er et nytt produkt"))
            updated.shouldNotBeNull()
            updated.title shouldBe "Dette er et nytt produkt"
            println(objectMapper.writeValueAsString(updated))
            val ids = productRepository.findIdsByStatusAndCreatedBy(status=ProductStatus.ACTIVE, HMDB)
            ids.size shouldBe 2
            productRepository.findByAgreementsJson("""[{"id": "$agreementId2"}]""").size shouldBe 1
        }
    }
}
