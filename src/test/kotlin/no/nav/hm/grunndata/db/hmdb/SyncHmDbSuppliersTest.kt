package no.nav.hm.grunndata.db.hmdb

import io.kotest.common.runBlocking
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import no.nav.hm.grunndata.db.hmdb.supplier.HmdbSupplierDTO
import no.nav.hm.grunndata.db.hmdb.supplier.SupplierSync
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import no.nav.hm.grunndata.rapid.dto.SupplierInfo
import no.nav.hm.grunndata.rapid.dto.SupplierStatus
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@MicronautTest
class SyncHmDbSuppliersTest(private val supplierSync: SupplierSync,
                            private val supplierRepository: SupplierRepository,
                            private val hmDbClient: HmDbClient) {

    @MockBean(RapidPushService::class)
    fun mockRapidService(): RapidPushService = mockk(relaxed = true)

    @MockBean(HmDbClient::class)
    fun mockHMDbClient(): HmDbClient = mockk(relaxed = true)

    @Test
    fun syncSupplierTest() = runBlocking {

        val supplier1 = supplierRepository.save(Supplier(name = "supplier 1",
            identifier = "HMDB-1", status = SupplierStatus.ACTIVE,
            info = SupplierInfo(email = "test1@test1")
        ))
        val supplier2 = supplierRepository.save(Supplier(name = "supplier 2",
            identifier = "HMDB-2", status = SupplierStatus.ACTIVE,
            info = SupplierInfo(email = "test2@test2")
        ))
        val supplier3 = supplierRepository.save(Supplier(name = "supplier 3",
            identifier = "HMDB-3", status = SupplierStatus.ACTIVE,
            info = SupplierInfo(email = "test3@test3")
        ))
        val supplier4 = supplierRepository.save(Supplier(name = "supplier 4",
            identifier = "HMDB-4", status = SupplierStatus.ACTIVE, createdBy = "REGISTER",
            updatedBy = "REGISTER",
            info = SupplierInfo(email = "test4@test4")
        ))
        every {
            runBlocking { hmDbClient.fetchAllSuppliers() }
        } answers {
            listOf(
                HmdbSupplierDTO(
                    adressid = 1,
                    adressnamn1 = "supplier 1",
                    postadress1 = "",
                    postnr = "",
                    postort = "",
                    telefon = "",
                    epost = "",
                    www = "",
                    landkod = "",
                    lastupdated = LocalDateTime.now(),
                    adrinsertdate = LocalDateTime.now()
                ),
                HmdbSupplierDTO(
                    adressid = 2,
                    adressnamn1 = "supplier 2",
                    postadress1 = "",
                    postnr = "",
                    postort = "",
                    telefon = "",
                    epost = "",
                    www = "",
                    landkod = "",
                    lastupdated = LocalDateTime.now(),
                    adrinsertdate = LocalDateTime.now()
                ),
                HmdbSupplierDTO(
                    adressid = 5,
                    adressnamn1 = "supplier 5",
                    postadress1 = "",
                    postnr = "",
                    postort = "",
                    telefon = "",
                    epost = "",
                    www = "",
                    landkod = "",
                    lastupdated = LocalDateTime.now(),
                    adrinsertdate = LocalDateTime.now()
                ),
                HmdbSupplierDTO(
                    adressid = 6,
                    adressnamn1 = "supplier 6",
                    postadress1 = "",
                    postnr = "",
                    postort = "",
                    telefon = "",
                    epost = "",
                    www = "",
                    landkod = "",
                    lastupdated = LocalDateTime.now(),
                    adrinsertdate = LocalDateTime.now()
                )
            )

        }
        supplierSync.syncAllSuppliers()
    }
}