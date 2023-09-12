package no.nav.hm.grunndata.db.hmdb

import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.hmdb.product.ProductSync
import no.nav.hm.grunndata.db.product.Product
import no.nav.hm.grunndata.db.product.ProductRepository
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.Attributes
import no.nav.hm.grunndata.rapid.dto.CompatibleWith
import no.nav.hm.grunndata.rapid.dto.SupplierInfo
import no.nav.hm.grunndata.rapid.dto.SupplierStatus
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class SyncProductTest(private val productSync: ProductSync,
                      private val supplierService: SupplierService,
                      private val productRepository: ProductRepository,
                      private val hmDbClient: HmDbClient) {

    @MockBean(RapidPushService::class)
    fun mockRapidService(): RapidPushService = mockk(relaxed = true)

    @MockBean(HmDbClient::class)
    fun mockHMDbClient(): HmDbClient = mockk(relaxed = true)

    //@Test ignore, just for integration
    fun syncProducts() {
        runBlocking { productSync.syncProducts() }
    }

    @Test
    fun syncProductStates() {
        runBlocking {
            val supplier = supplierService.save(
                Supplier(name = "supplier 1", identifier = "unik-identifier",
                status = SupplierStatus.ACTIVE, info = SupplierInfo(email = "test@test")
                )
            )
            productRepository.save(
                Product(
                supplierId = supplier.id, identifier = "HMDB-123", title = "Dette er et produkt", articleName = "Produkt 1",
                supplierRef = "123", isoCategory = "123456",
                attributes = Attributes (
                    manufacturer =  "Samsung",  compatibleWidth = CompatibleWith(ids = listOf(UUID.randomUUID())
                )))
            )
            productRepository.save(
                Product(
                supplierId = supplier.id, identifier = "HMDB-124", title = "Dette er et produkt2", articleName = "Produkt 2",
                supplierRef = "124", isoCategory = "123456",
                attributes = Attributes (
                    manufacturer =  "Samsung",  compatibleWidth = CompatibleWith(ids = listOf(UUID.randomUUID()))))
            )
            val product3 = productRepository.save(
                Product(
                    supplierId = supplier.id, identifier = "HMDB-125", title = "Dette er et produkt2", articleName = "Produkt 2",
                    supplierRef = "125", isoCategory = "123456",
                    attributes = Attributes (
                        manufacturer =  "Samsung",  compatibleWidth = CompatibleWith(ids = listOf(UUID.randomUUID()))))
            )
            every {
                hmDbClient.fetchProductsIdActive()
            } answers {
                listOf(
                    123, 124, 126, 127
                )
            }
            productSync.syncHMDBProductStates()
        }
    }
}
