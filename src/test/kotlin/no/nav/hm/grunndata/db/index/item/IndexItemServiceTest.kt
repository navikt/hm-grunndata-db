package no.nav.hm.grunndata.db.index.item

import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.index.supplier.SupplierDoc
import no.nav.hm.grunndata.rapid.dto.SupplierStatus
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@MicronautTest
class IndexItemServiceTest(private val indexItemService: IndexItemService) {

    @Test
    fun  testIndexItemService() {
        val supplierDoc = SupplierDoc(
            id = UUID.randomUUID().toString(),
            identifier = "identifier",
            status = SupplierStatus.ACTIVE,
            name = "name",
            address = "address",
            postNr = "1234",
            postLocation = "Oslo",
            countryCode = "NO",
            email = "email",
            phone = "12345678",
            homepage = "http://homepage.no",
            createdBy = "createdBy",
            updatedBy = "updatedBy",
            created = LocalDateTime.now(),
            updated = LocalDateTime.now(),
        )
        runBlocking {
            indexItemService.saveIndexItem(supplierDoc, IndexType.SUPPLIER, indexSettingsMap[IndexType.SUPPLIER]!!.aliasIndexName)
            indexItemService.processPendingIndexItems(size=1000) shouldBe 1
            indexItemService.deleteOldIndexItems() shouldBe 0
        }
    }
}