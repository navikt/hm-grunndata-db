package no.nav.hm.grunndata.db.product

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.data.exceptions.DataAccessException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierInfo
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import org.junit.jupiter.api.Test

@MicronautTest
class ProductRepositoryTest(private val productRepository: ProductRepository,
                            private val supplierRepository: SupplierRepository) {

    @Test
    fun readSavedDb() {
        runBlocking {
            val supplier = supplierRepository.save(Supplier(name = "supplier 1", identifier = "unik-identifier", info = SupplierInfo(email = "test@test")))
            val product = productRepository.save(Product(
                supplierId = supplier.id, identifier = "123", title = "Dette er et produkt", supplierRef = "123", isoCategory = "123456",
                description = Description(name = "Produkt 1")
            ))
            val db = productRepository.findById(product.id)
            db.shouldNotBeNull()
            db.supplierId shouldBe product.supplierId
            db.title shouldBe product.title
            val updated = productRepository.update(db.copy(title = "Dette er et nytt produkt"))
            updated.shouldNotBeNull()
            updated.title shouldBe "Dette er et nytt produkt"
        }
    }
}
