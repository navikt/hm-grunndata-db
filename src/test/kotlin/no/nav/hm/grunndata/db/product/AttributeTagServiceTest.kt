package no.nav.hm.grunndata.db.product

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class AttributeTagServiceTest(private val attributeTagService: AttributeTagService) {

    @Test
    fun attributeTagTest() {
        val product = ProductDTO (id = UUID.randomUUID(),
            supplierId = UUID.randomUUID(), hmsArtNr = "255734", identifier = "123", title = "Dette er et produkt",
            supplierRef = "123", isoCategory = "123456",
            attributes = mapOf(
                AttributeNames.articlename to  "Produkt 1",
                AttributeNames.manufacturer to  "Samsung",
                AttributeNames.compatible to listOf("produkt 2", "product 3"))
        )
        val product2 = ProductDTO (id = UUID.randomUUID(),
            supplierId = UUID.randomUUID(), hmsArtNr = "12345A", identifier = "123", title = "Dette er et produkt",
            supplierRef = "123", isoCategory = "123456",
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