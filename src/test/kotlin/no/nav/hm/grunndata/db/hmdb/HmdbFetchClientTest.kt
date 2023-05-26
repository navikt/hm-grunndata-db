package no.nav.hm.grunndata.db.hmdb

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import no.nav.hm.grunndata.db.agreement.Agreement
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.hmdb.product.HmDBProductMapper
import no.nav.hm.grunndata.db.hmdb.product.HmDbProductBatchDTO
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.db.product.Product
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@MicronautTest
class HmdbFetchClientTest(private val fetchClient: HmDbClient,
                          private val hmDBProductMapper: HmDBProductMapper,
                          private val objectMapper: ObjectMapper) {

    @MockBean(SupplierService::class)
    fun supplierService(): SupplierService = mockk(relaxed = true)

    @MockBean(AgreementService::class)
    fun agreementService(): AgreementService = mockk(relaxed = true)

    @MockBean(IsoCategoryService::class)
    fun isoCategoryService(): IsoCategoryService = mockk(relaxed = true)

    // integration test
    //@Test
    fun testHmdbFetchClient() {
        every {
            supplierService().findByIdentifier(any())
        } answers {
            Supplier(identifier = "123", name = "123", info = SupplierInfo(), status = SupplierStatus.ACTIVE)
        }
        every {
            agreementService().findByIdentifier(any())
        } answers {
            Agreement(id = UUID.randomUUID(), identifier = "HMDB-123", title = "Rammeavtale Rullestoler", resume = "En kort beskrivelse", reference="23-10234",
                text = "En lang beskrivelse 1", published = LocalDateTime.now(), expired = LocalDateTime.now().plusYears(3),
                posts = listOf(AgreementPost(title = "Post 1", identifier = "HMDB-321", nr = 1, description = "En beskrive av posten")))
        }
        val batch = fetchClient.fetchProducts(LocalDateTime.of(2009,9,25,0,0),
            LocalDateTime.of(2009,10,25,0,0))
        val products = extractProductBatch(batch!!)
        println(batch.products.size)
        //println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(products))
        val last = products.last()
        println(last.updated)
    }

    private fun extractProductBatch(batch: HmDbProductBatchDTO): List<Product> {
        return batch.products.map { prod ->
            println("mapping ${prod.artid} with ${prod.achange}")
            hmDBProductMapper.mapProduct(prod, batch)
        }.sortedBy { it.updated }
    }
}
