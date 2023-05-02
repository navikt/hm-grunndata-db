package no.nav.hm.grunndata.db.hmdb

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.any
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.MockKSettings.relaxed
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import no.nav.hm.grunndata.db.agreement.Agreement
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.hmdb.product.HmDBProductMapper
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.AgreementPost
import no.nav.hm.grunndata.rapid.dto.SupplierInfo
import no.nav.hm.grunndata.rapid.dto.SupplierStatus
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

    //@Test integration test
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
            LocalDateTime.of(2009,10,26,0,0))
        val products = batch!!.products.map { prod ->
             hmDBProductMapper.mapProduct(prod, batch)
        }.sortedBy { it.updated }
        //println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(products))
        val last = products.last()
        println(last.updated)
    }
}
