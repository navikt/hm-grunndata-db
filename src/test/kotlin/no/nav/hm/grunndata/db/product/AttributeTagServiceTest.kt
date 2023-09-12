package no.nav.hm.grunndata.db.product

import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.db.GDB
import no.nav.hm.grunndata.rapid.dto.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@MicronautTest
class AttributeTagServiceTest(private val attributeTagService: AttributeTagService) {

    @Test
    fun attributeTagTest() {
        val supplier = SupplierDTO(id = UUID.randomUUID(), identifier = "341", name = "leverandør 1", info = SupplierInfo(),
            createdBy = GDB, updatedBy = GDB, updated = LocalDateTime.now(), created = LocalDateTime.now())
        val product = Product (id = UUID.randomUUID(),
            supplierId = supplier.id , hmsArtNr = "267914", identifier = "123", title = "Dette er et produkt",
            articleName = "Product 1", supplierRef = "123", isoCategory = "123456",
            attributes = Attributes (
                manufacturer =  "Samsung",
                compatibleWidth = CompatibleWith(ids = listOf(UUID.randomUUID()))
            ),
            createdBy = GDB,
            updatedBy = GDB
        )
        val product2 = Product (id = UUID.randomUUID(),
            supplierId = supplier.id, hmsArtNr = "12345A", identifier = "123", title = "Dette er et produkt",
            articleName = "Produkt1", supplierRef = "1234", isoCategory = "123456",
            attributes = Attributes (
                manufacturer = "Samsung",
                compatibleWidth = CompatibleWith(ids = listOf(UUID.randomUUID())),
                bestillingsordning= true
            ),
            createdBy = GDB,
            updatedBy = GDB
        )
        val withBestillingsordning = attributeTagService.addBestillingsordningAttribute(product)
        val withNoBestillingsordning = attributeTagService.addBestillingsordningAttribute(product2)
        withBestillingsordning.attributes.bestillingsordning shouldBe true
        withNoBestillingsordning.attributes.bestillingsordning shouldBe false
    }
}
