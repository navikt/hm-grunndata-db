package no.nav.hm.grunndata.db.hmdb

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.common.runBlocking
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.db.search.ProductIndexer
import org.junit.jupiter.api.Test
import java.io.File

@MicronautTest
class HmDbProduktBatchTest(private val hmDbProduktBatchRepository: HmDbProduktBatchRepository,
                           private val leverandorerBatchRepository: HmDbLeverandorerBatchRepository,
                           private val objectMapper: ObjectMapper, private val productIndexer: ProductIndexer
) {

    @Test
    fun hmdbProduktBatchTest() {
        val leverandorBatch = objectMapper.readValue(File("tmp/dumps/leverandor.json"),
            HmDbLeverandorerBatchDTO::class.java)
        leverandorBatch.leverandorer.forEach {
            println("leverandør ${it.leverandorid} ${it.leverandornavn}")
        }
        println("Leverandør total: ${leverandorBatch.leverandorer.size}")
        val produkterBatch = objectMapper.readValue(
            File("tmp/dumps/produkter.json"),
            HmDbProduktBatchDTO::class.java)

        runBlocking {
            leverandorerBatchRepository.save(leverandorBatch.toEntity())
            hmDbProduktBatchRepository.save(produkterBatch.toEntity())

            val dbLev = leverandorerBatchRepository.findFirstOrderByCreatedDesc()
            dbLev!!.leverandorer.size shouldBe leverandorBatch.leverandorer.size

            val dbProdukt = hmDbProduktBatchRepository.findFirstOrderByCreatedDesc()
            dbProdukt!!.produkter.size shouldBe  produkterBatch.produkter.size
            dbProdukt.tekniskeData.size shouldBe produkterBatch.tekniskeData.size

        }
    }
}

