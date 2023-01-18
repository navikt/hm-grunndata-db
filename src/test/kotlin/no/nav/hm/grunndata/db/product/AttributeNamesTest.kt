package no.nav.hm.grunndata.db.product

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import no.nav.hm.grunndata.db.product.AttributeNames.*
import org.junit.jupiter.api.Test

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