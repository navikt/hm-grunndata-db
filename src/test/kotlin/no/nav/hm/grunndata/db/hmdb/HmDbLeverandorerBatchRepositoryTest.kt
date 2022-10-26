package no.nav.hm.grunndata.db.hmdb

import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

@MicronautTest
class HmDbLeverandorerBatchRepositoryTest(private val repository: HmDbLeverandorerBatchRepository) {

    @Test
    fun readSaveDb() {
        val lev = LeverandorDTO(
            leverandorid = "123",
            leverandornavn= "Tester",
            adresse="envei 1",
            postnummer="1234",
            poststed="posted",
            telefon="1234567",
            epost= "epost@epost.test",
            www = "www.hjemmeside.no",
            landkode = "no")
        val batch = HmDbLeverandorerBatch(leverandorer = listOf(lev), md5 = "12345")
        runBlocking {
            val saved = repository.save(batch)
            val db = repository.findById(saved.id)
            db.shouldNotBeNull()
            db.leverandorer.size shouldBe 1
        }
    }
}
