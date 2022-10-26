package no.nav.hm.grunndata.db.hmdb

import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

@MicronautTest
class HmDbProduktBatchRepositoryTest(private val repository: HmDbProduktBatchRepository) {

    @Test
    fun readSaveDb() {
        val batch = HmDbProduktBatch(produkter = emptyList(), tekniskeData = emptyList())
        runBlocking {
            val saved = repository.save(batch)
            val db = repository.findById(saved.id)
            db.shouldNotBeNull()

        }
    }
}
