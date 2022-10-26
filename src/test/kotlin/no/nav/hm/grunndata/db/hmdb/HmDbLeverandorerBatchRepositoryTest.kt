package no.nav.hm.grunndata.db.hmdb

import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.flow.*
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
            landkode = "no"
        )
        val batch = HmDbLeverandorerBatch(leverandorer = listOf(lev))
        runBlocking {
            val saved = repository.save(batch)
            val db = repository.findById(saved.id)
            db.shouldNotBeNull()
            db.leverandorer.size shouldBe 1
            db.created shouldBe batch.created
        }
        val lev2 = LeverandorDTO(
            leverandorid = "1234",
            leverandornavn= "Tester",
            adresse="envei 1",
            postnummer="1234",
            poststed="posted",
            telefon="1234567",
            epost= "epost@epost.test",
            www = "www.hjemmeside.no",
            landkode = "no"
        )
        val batch2 = HmDbLeverandorerBatch(leverandorer = listOf(lev2))
        runBlocking {
            val saved = repository.save(batch2)
            val db = repository.findById(saved.id)
            db.shouldNotBeNull()
            db.leverandorer.size shouldBe 1
            db.created shouldBe batch2.created
            val lastIsFirst = repository.findFirstOrderByCreatedDesc()
            lastIsFirst.shouldNotBeNull()
            lastIsFirst.leverandorer[0].leverandorid shouldBe "1234"
            val list = repository.findAll().toList()
            list.size shouldBe  2
        }
    }
}
