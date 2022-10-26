package no.nav.hm.grunndata.db.hmdb

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post

@Controller("/api/v1/hmdbimport")
class HmDbImportApi(private val hmDbProduktBatchRepository: HmDbProduktBatchRepository,
                    private val hmDbLeverandorerBatchRepository: HmDbLeverandorerBatchRepository) {

    @Post("/products")
    suspend fun importProducts(@Body produktBatch: HmDbProduktBatchDTO) {
        hmDbProduktBatchRepository.save(produktBatch.toEntity())
    }

    @Post("/suppliers")
    suspend fun importSuppliers(@Body leverandorerBatch: HmDbLeverandorerBatchDTO) {
        hmDbLeverandorerBatchRepository.save(leverandorerBatch.toEntity())
    }
}
