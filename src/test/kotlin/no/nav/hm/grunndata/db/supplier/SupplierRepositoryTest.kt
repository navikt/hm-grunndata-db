package no.nav.hm.grunndata.db.supplier

import io.kotest.common.runBlocking
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.db.hmdb.HmdbSupplierDTO
import no.nav.hm.grunndata.db.hmdb.toSupplier
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class SupplierRepositoryTest(private val repository: SupplierRepository) {

    @Test
    fun readSave() {
        val legacy = HmdbSupplierDTO(adressid=1000L, adressnamn1="legacy company", postadress1= "postaddresse",
            postnr="0001", postort= "poststed", telefon="12345678", epost ="epost@epost.test", www="www.homepage.com",
            landkod="no", lastupdated = LocalDateTime.now(), adrinsertdate=LocalDateTime.now())
        val supplier = legacy.toSupplier()
        runBlocking {
            val saved = repository.save(supplier)
            val db = repository.findById(saved.id)
            db.shouldNotBeNull()
            db.name shouldBe "legacy company"
            db.identifier shouldBe "hmdbid-1000"
            db.info.email shouldBe "epost@epost.test"
        }

    }
}
