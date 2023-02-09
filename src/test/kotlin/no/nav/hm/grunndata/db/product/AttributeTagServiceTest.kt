package no.nav.hm.grunndata.db.product

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.db.GDB
import no.nav.hm.grunndata.db.supplier.SupplierDTO
import no.nav.hm.grunndata.db.supplier.SupplierInfo
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@MicronautTest
class AttributeTagServiceTest(private val attributeTagService: AttributeTagService) {

    @Test
    fun attributeTagTest() {
        val supplier = SupplierDTO(id = UUID.randomUUID(), identifier = "341", name = "leverandør 1", info = SupplierInfo(),
            createdBy = GDB, updatedBy = GDB, updated = LocalDateTime.now(), created = LocalDateTime.now())
        val product = ProductDTO (id = UUID.randomUUID(),
            supplier = supplier , hmsArtNr = "255734", identifier = "123", title = "Dette er et produkt",
            supplierRef = "123", isoCategory = "123456",
            attributes = mapOf(
                AttributeNames.articlename to  "Produkt 1",
                AttributeNames.manufacturer to  "Samsung",
                AttributeNames.compatible to listOf("produkt 2", "product 3"))
        )
        val product2 = ProductDTO (id = UUID.randomUUID(),
            supplier = supplier, hmsArtNr = "12345A", identifier = "123", title = "Dette er et produkt",
            supplierRef = "1234", isoCategory = "123456",
            attributes = mapOf(
                AttributeNames.articlename to  "Produkt 1",
                AttributeNames.manufacturer to  "Samsung",
                AttributeNames.compatible to listOf("produkt 2", "product 3"),
                AttributeNames.bestillingsordning to true)
        )
        val withBestillingsordning = attributeTagService.addBestillingsordningAttribute(product)
        val withNoBestillingsordning = attributeTagService.addBestillingsordningAttribute(product2)
        withBestillingsordning.attributes[AttributeNames.bestillingsordning] shouldBe true
        withNoBestillingsordning.attributes[AttributeNames.bestillingsordning].shouldBeNull()
    }
}