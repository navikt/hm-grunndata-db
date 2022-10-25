package no.nav.hm.grunndata.db.hmdb

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post

@Controller("/api/v1/hmdbimport")
class HmDbImportApi(private val hmDbProduktBatchRepository: HmDbProduktBatchRepository) {

    @Post("/products")
    suspend fun importProducts(@Body produktBatch: HmDbProduktBatch) {
        hmDbProduktBatchRepository.save(produktBatch)
    }
}
