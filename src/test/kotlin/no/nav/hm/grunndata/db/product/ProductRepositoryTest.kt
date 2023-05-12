package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.*
import org.junit.jupiter.api.Test

@MicronautTest
class ProductRepositoryTest(private val productRepository: ProductRepository,
                            private val supplierService: SupplierService,
                            private val objectMapper: ObjectMapper) {

    @Test
    fun readSavedDb() {
        runBlocking {
            val supplier = supplierService.save(Supplier(name = "supplier 1", identifier = "unik-identifier",
                status = SupplierStatus.ACTIVE, info = SupplierInfo(email = "test@test")))
            val product = productRepository.save(Product(
                supplierId = supplier.id, identifier = "123", title = "Dette er et produkt", articleName = "Produkt 1",
                supplierRef = "123", isoCategory = "123456",
                attributes = Attributes (
                    manufacturer =  "Samsung",  compatible = listOf(CompatibleAttribute(hmsArtNr = "1")))
            ))
            val product2 = productRepository.save(Product(
                supplierId = supplier.id, identifier = "124", title = "Dette er et produkt2", articleName = "Produkt 2",
                supplierRef = "124", isoCategory = "123456",
                attributes = Attributes (
                    manufacturer =  "Samsung",  compatible = listOf(CompatibleAttribute(hmsArtNr = "2")))
            ))
            val db = productRepository.findById(product.id)
            db.shouldNotBeNull()
            db.supplierId shouldBe product.supplierId
            db.title shouldBe product.title
            db.articleName shouldBe "Produkt 1"
            val updated = productRepository.update(db.copy(title = "Dette er et nytt produkt"))
            updated.shouldNotBeNull()
            updated.title shouldBe "Dette er et nytt produkt"
            println(objectMapper.writeValueAsString(updated))
            val ids = productRepository.findIdsByStatus(status=ProductStatus.ACTIVE)
            ids.size shouldBe 2
            ids.map {
                println(it.identifier)
            }
        }
    }
}
