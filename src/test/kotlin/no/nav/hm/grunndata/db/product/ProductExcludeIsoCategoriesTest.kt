package no.nav.hm.grunndata.db.product

import io.kotest.common.runBlocking
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.Attributes
import no.nav.hm.grunndata.rapid.dto.CompatibleWith
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.dto.SupplierInfo
import no.nav.hm.grunndata.rapid.dto.SupplierStatus
import org.junit.jupiter.api.Test
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import java.util.*

@MicronautTest
class ProductExcludeIsoCategoriesTest {

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var productService: ProductService

    @Inject
    lateinit var supplierService: SupplierService

    @Test
    fun `should exclude products with specified iso categories`() {
        runBlocking {
            val excludedCodes = listOf("09540601", "09540901", "09540301")
            val supplier = supplierService.save(
                Supplier(
                    name = "supplier-x", identifier = "supplier-x", status = SupplierStatus.ACTIVE, info = SupplierInfo(email = "a@b.no")
                )
            )
            val p1 = productRepository.save(
                Product(
                    supplierId = supplier.id,
                    identifier = "p1", title = "Product 1", articleName = "Prod 1", supplierRef = "p1",
                    isoCategory = excludedCodes.first(), seriesUUID = UUID.randomUUID(), seriesId = null, seriesIdentifier = null,
                    attributes = Attributes(manufacturer = "Manu", compatibleWith = CompatibleWith()), status = ProductStatus.ACTIVE
                )
            )
            val p2 = productRepository.save(
                Product(
                    supplierId = supplier.id,
                    identifier = "p2", title = "Product 2", articleName = "Prod 2", supplierRef = "p2",
                    isoCategory = "12345678", seriesUUID = UUID.randomUUID(), seriesId = null, seriesIdentifier = null,
                    attributes = Attributes(manufacturer = "Manu", compatibleWith = CompatibleWith()), status = ProductStatus.ACTIVE
                )
            )
            val page = productService.findProducts(
                ProductCriteria(excludeIsoCategories = excludedCodes),
                Pageable.from(0,10, Sort.of(Sort.Order.asc("id")))
            )
            val ids = page.content.map { it.id }
            ids.shouldContain(p2.id)
            ids.shouldNotContain(p1.id)
            page.totalSize shouldBe 1
        }
    }
}
