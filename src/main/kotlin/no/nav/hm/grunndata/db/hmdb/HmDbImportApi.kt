package no.nav.hm.grunndata.db.hmdb

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import org.slf4j.LoggerFactory

@Controller("/api/v1/hmdbimport")
class HmDbImportApi(private val hmDbProduktBatchRepository: HmDbProduktBatchRepository,
                    private val hmDbLeverandorerBatchRepository: HmDbLeverandorerBatchRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(HmDbImportApi::class.java)
    }
    @Post("/products")
    suspend fun importProducts(@Body produktBatch: HmDbProduktBatchDTO) {
        LOG.info("Received import for products ${produktBatch.produkter.size}")
        hmDbProduktBatchRepository.save(produktBatch.toEntity())
    }

    @Post("/suppliers")
    suspend fun importSuppliers(@Body leverandorerBatch: HmDbLeverandorerBatchDTO) {
        LOG.info("Received import for suppliers ${leverandorerBatch.leverandorer.size}")
        hmDbLeverandorerBatchRepository.save(leverandorerBatch.toEntity())
    }
}
