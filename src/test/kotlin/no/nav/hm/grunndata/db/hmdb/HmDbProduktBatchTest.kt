package no.nav.hm.grunndata.db.hmdb

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.db.search.ProductIndexer
import no.nav.hm.grunndata.db.search.toDoc
import org.junit.jupiter.api.Test
import java.io.File

@MicronautTest
class HmDbProduktBatchTest(private val hmDbProduktBatchRepository: HmDbProduktBatchRepository,
                           private val objectMapper: ObjectMapper, private val productIndexer: ProductIndexer
) {

    @Test
    fun hmdbProduktBatchTest() {
        val leverandor = objectMapper.readValue(File("tmp/dumps/leverandor.json"),
            HmDbLeverandorerBatch::class.java)
        leverandor.leverandorer.forEach {
        }
        println("Leverandør total: ${leverandor.leverandorer.size}")
        val batch = objectMapper.readValue(
            File("tmp/dumps/leverandor.json"),
            HmDbProduktBatch::class.java)
        val productList = batch.toProductList()
        println("Product total ${productList.size}")
        //productIndexer.index(productList.map { it.toDoc() })
        //val productseries = batch.produkter.groupBy { it.prodid }
//        run looper@ {
//            productseries.entries.forEach { (key, value) ->
//                if (value.size>3 && value[0].aout==false && value[0].hasanbud == true) {
//                    value.forEach {
//                        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(it))
//                    }
//                    return@looper
//                }
//            }
//        }

    }
}
