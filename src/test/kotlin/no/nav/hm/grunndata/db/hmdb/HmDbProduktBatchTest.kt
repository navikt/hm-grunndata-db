package no.nav.hm.grunndata.db.hmdb

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.common.runBlocking
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
        val leverandor = objectMapper.readValue(File("tmp/dumps/leverandor.json"),
            HmDbLeverandorerBatchDTO::class.java)
        leverandor.leverandorer.forEach {
            println("leverandør ${it.leverandorid} ${it.leverandornavn}")
        }
        println("Leverandør total: ${leverandor.leverandorer.size}")
        val batchProdukter = objectMapper.readValue(
            File("tmp/dumps/produkter.json"),
            HmDbProduktBatchDTO::class.java)

        runBlocking {
            leverandorerBatchRepository.save(leverandor.toEntity())
            hmDbProduktBatchRepository.save(batchProdukter.toEntity())
        }
    }
}

