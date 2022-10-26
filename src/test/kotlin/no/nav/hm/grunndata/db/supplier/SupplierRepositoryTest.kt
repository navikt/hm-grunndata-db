package no.nav.hm.grunndata.db.supplier

import io.kotest.common.runBlocking
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

@MicronautTest
class SupplierRepositoryTest(private val repository: SupplierRepository) {

    @Test
    fun readSave() {
        val legacy = Supplier(id=100, hmdbId = 100, name="Leverandør AS", info=SupplierInfo(address = "et sted", email = "epost@epost.test",
            phone="123456", homepage = "www.homepage.no"))
        val supplier = Supplier(name="New Generation", info=SupplierInfo(address = "et sted 2", email = "epost2@epost2.test",
            phone="1234567", homepage = "www.homepage.no"))
        runBlocking {
            repository.insertLegacy(legacy)
            val db = repository.findById(100)
            db.shouldNotBeNull()
            db.name shouldBe "Leverandør AS"
            db.info.email shouldBe "epost@epost.test"
            val saved = repository.save(supplier)
            saved.id shouldBeGreaterThan -1
            val find = repository.findById(saved.id)
            find.shouldNotBeNull()
            find.info.address shouldBe "et sted 2"
        }

    }
}
