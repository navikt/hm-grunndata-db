package no.nav.hm.grunndata.db.product

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import no.nav.hm.grunndata.db.product.AttributeNames.*

class AttributeNamesTest {

    @Test
    fun attributNameTest() {
        val attributes: Map<String, Any> = mapOf(Pair(compatibilty.name, listOf("Product 1", "Product 2")), Pair(shortdescription.name,"short description"),
        Pair(text.name, "A long description of product"))
        attributes.keys.map {
            enumContains<AttributeNames>(it)
        }.shouldNotContain(false)

        val attributesNotSupported : Map<String, Any> = mapOf(Pair(compatibilty.name, listOf("Product 1", "Product 2")), Pair(shortdescription.name,"short description"),
            Pair(text.name, "A long description of product"), Pair("notsupported", "test"))

        attributesNotSupported.keys.map { enumContains<AttributeNames>(it) }.shouldContain(false)
    }
}