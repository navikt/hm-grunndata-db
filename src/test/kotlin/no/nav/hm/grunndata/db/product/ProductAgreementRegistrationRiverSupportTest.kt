package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.common.runBlocking
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.mockk
import no.nav.hm.grunndata.db.agreement.Agreement
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.AgreementPost
import no.nav.hm.grunndata.rapid.dto.AgreementStatus
import no.nav.hm.grunndata.rapid.dto.Attributes
import no.nav.hm.grunndata.rapid.dto.CompatibleWith
import no.nav.hm.grunndata.rapid.dto.ProductAgreementRegistrationRapidDTO
import no.nav.hm.grunndata.rapid.dto.SupplierInfo
import no.nav.hm.grunndata.rapid.dto.SupplierStatus
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import no.nav.hm.grunndata.rapid.dto.ProductAgreementStatus

@MicronautTest
class ProductAgreementRegistrationRiverSupportTest(
    private val supplierService: SupplierService,
    private val agreementService: AgreementService,
    private val productService: ProductService,
    private val support: ProductAgreementRegistrationRiverSupport,
    private val objectMapper: ObjectMapper
) {

    @MockBean(RapidPushService::class)
    fun mockRapidService(): RapidPushService = mockk(relaxed = true)


    @Test
    fun testProductAgreementRegistrationRiver() {
        val agreementId = UUID.randomUUID()
        val agreementId2 = UUID.randomUUID()
        val agreement = Agreement(id = agreementId, identifier = "HMDB-123", title = "Rammeavtale Rullestoler", resume = "En kort beskrivelse", reference="23-10234",
            text = "En lang beskrivelse 1", published = LocalDateTime.now(), expired = LocalDateTime.now().plusYears(3),
            posts = listOf(
                AgreementPost(title = "Post 1", identifier = "HMDB-321", nr = 1, description = "En beskrive av posten"),
                AgreementPost(title = "Post 2", identifier = "HMDB-322", nr = 2, description = "En beskrive av posten")
            ))

        val pastAgreement = Agreement(id = agreementId2, identifier = "HMDB-124", title = "Rammeavtale Rullestoler",
            resume = "En kort beskrivelse", reference="23-10235", status = AgreementStatus.INACTIVE,
            expired = LocalDateTime.now().minusYears(1),
            text = "En lang beskrivelse 1", published = LocalDateTime.now(),
            posts = listOf(
                AgreementPost(title = "Post 1", identifier = "HMDB-333", nr = 1, description = "En beskrive av posten"),
                AgreementPost(title = "Post 2", identifier = "HMDB-444", nr = 2, description = "En beskrive av posten")
            ))

        val productAgreement1 = ProductAgreement(
            id = agreement.id, identifier = agreement.identifier, reference = agreement.reference,
            rank = 1, postNr = 1, postIdentifier = agreement.posts[0].identifier, status = ProductAgreementStatus.ACTIVE,
            published = LocalDateTime.now(), expired = LocalDateTime.now().plusYears(3)
        )

        val productAgreement2 = ProductAgreement(
            id = agreement.id, identifier = agreement.identifier, reference = agreement.reference,
            rank = 2, postNr = 2, postIdentifier = agreement.posts[1].identifier, status =ProductAgreementStatus.ACTIVE,
            published = LocalDateTime.now(), expired = LocalDateTime.now().plusYears(3)
        )

        val pastProductAgreement = ProductAgreement(
            id = pastAgreement.id, identifier = pastAgreement.identifier, reference = pastAgreement.reference,
            rank = 2, postNr = 2, postIdentifier = pastAgreement.posts[1].identifier, status = ProductAgreementStatus.INACTIVE,
            published = LocalDateTime.now(), expired = LocalDateTime.now().minusDays(1)
        )

        val supplier = supplierService.save(
            Supplier(
                name = "supplier 1", identifier = "unik-identifier",
                status = SupplierStatus.ACTIVE, info = SupplierInfo(email = "test@test")
            )
        )
        runBlocking {
            agreementService.save(agreement)
            val product = productService.saveAndPushTokafka(
                Product(
                    supplierId = supplier.id,
                    identifier = "123",
                    title = "Dette er et produkt",
                    articleName = "Produkt 1",
                    supplierRef = "J71001-15",
                    isoCategory = "123456",
                    agreements = setOf(productAgreement2),
                    pastAgreements = setOf(pastProductAgreement),
                    seriesUUID = UUID.randomUUID(),
                    attributes = Attributes(
                        manufacturer = "Samsung", compatibleWidth = CompatibleWith(seriesIds = setOf(UUID.randomUUID()))
                    )
                ), "test-event"
            )
            val productAgreement = objectMapper.readValue(
                """
             {                                                       
               "id" : "5bc138f6-c333-4c74-9329-37d54b9432bb",        
               "productId" : "${product.id}", 
               "seriesUuid" : "${product.seriesUUID}",
               "title" : "VELA Tango 700 Aktiv",                     
               "articleName" : "VELA Tango 700 Aktiv Stor",          
               "supplierId" : "${supplier.id}",
               "supplierRef" : "J71001-15",                          
               "hmsArtNr" : "323901",                                
               "agreementId" : "$agreementId",
               "reference" : "23-10234",                            
               "post" : ${agreement.posts.get(0).nr},                                           
               "rank" : 1,                                           
               "postId" : "${agreement.posts.get(0).id}",    
               "status" : "ACTIVE",                                  
               "createdBy" : "REGISTER",                             
               "created" : "2023-02-15T10:47:00.778391",             
               "updated" : "2024-05-21T10:25:03.251",                
               "published" : "2022-06-01T00:00:00",                  
               "expired" : "2024-06-01T00:00:00",                    
               "updatedByUser" : "system"                            
             }
            """.trimIndent(), ProductAgreementRegistrationRapidDTO::class.java
            )
            val merged = support.mergeAgreementInProduct(product, productAgreement)
            merged.agreements.size shouldBe 2
            println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(merged))
        }


    }
}