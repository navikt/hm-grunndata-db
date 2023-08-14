package no.nav.hm.grunndata.db.supplier

import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.mockk
import no.nav.hm.grunndata.db.hmdb.supplier.HmdbSupplierDTO
import no.nav.hm.grunndata.db.hmdb.supplier.toSupplier
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class SupplierServiceTest(private val supplierService: SupplierService) {


    @MockBean(RapidPushService::class)
    fun mockRapidService(): RapidPushService = mockk(relaxed = true)

    @Test
    fun readSave() {
        val legacy = HmdbSupplierDTO(adressid=1000L, adressnamn1="legacy company", postadress1= "postaddresse",
            postnr="0001", postort= "poststed", telefon="12345678", epost ="epost@epost.test", www="www.homepage.com",
            landkod="no", lastupdated = LocalDateTime.now(), adrinsertdate=LocalDateTime.now())
        val legacy2 = HmdbSupplierDTO(adressid=1001L, adressnamn1="legacy company2", postadress1= "postaddresse2",
            postnr="0002", postort= "poststed", telefon="12345679", epost ="epost2@epost.test", www="www.homepage2.com",
            landkod="no", lastupdated = LocalDateTime.now(), adrinsertdate=LocalDateTime.now())
        val supplier = legacy.toSupplier()
        val supplier2 = legacy2.toSupplier()
        runBlocking {
            val saved = supplierService.save(supplier)
            val saved2 = supplierService.save(supplier2)
            val db = supplierService.findById(saved.id)
            val db2 = supplierService.findById(saved2.id)
            val db3 = supplierService.findById(saved2.id)
            db.shouldNotBeNull()
            db.name shouldBe "legacy company"
            db.identifier shouldBe "HMDB-1000"
            db.info.email shouldBe "epost@epost.test"
            db2.shouldNotBeNull()
            db2.identifier shouldBe "HMDB-1001"
            db3!!.identifier shouldBe "HMDB-1001"
        }

    }
}
