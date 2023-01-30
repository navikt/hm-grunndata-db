package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.common.runBlocking
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.db.product.AttributeNames.*
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierInfo
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import no.nav.hm.grunndata.db.supplier.SupplierStatus
import org.junit.jupiter.api.Test

@MicronautTest
class ProductRepositoryTest(private val productRepository: ProductRepository,
                            private val supplierRepository: SupplierRepository, private val objectMapper: ObjectMapper) {

    @Test
    fun readSavedDb() {
        runBlocking {
            val supplier = supplierRepository.save(Supplier(name = "supplier 1", identifier = "unik-identifier",
                status = SupplierStatus.ACTIVE, info = SupplierInfo(email = "test@test")))
            val product = productRepository.save(Product(
                supplierId = supplier.id, identifier = "123", title = "Dette er et produkt", supplierRef = "123", isoCategory = "123456",
                attributes = mapOf(articlename to  "Produkt 1",
                    manufacturer to  "Samsung",  compatible to listOf("produkt 2", "product 3"))
            ))
            val db = productRepository.findById(product.id)
            db.shouldNotBeNull()
            db.supplierId shouldBe product.supplierId
            db.title shouldBe product.title
            db.attributes shouldContain Pair(articlename, "Produkt 1")
            val updated = productRepository.update(db.copy(title = "Dette er et nytt produkt"))
            updated.shouldNotBeNull()
            updated.title shouldBe "Dette er et nytt produkt"
            println(objectMapper.writeValueAsString(updated))
        }
    }
}
