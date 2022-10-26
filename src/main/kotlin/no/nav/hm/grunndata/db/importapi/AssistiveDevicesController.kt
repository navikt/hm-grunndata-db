package no.nav.hm.grunndata.db.importapi

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import no.nav.hm.grunndata.db.search.ProductIndexer
import no.nav.hm.grunndata.db.search.toDoc
import org.slf4j.LoggerFactory

@Controller("/api/v1/assistive/devices")
class AssistiveDevicesController(private val objectMapper: ObjectMapper, private val productIndexer: ProductIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AssistiveDevicesController::class.java)
    }

    @Post(value = "/{supplierId}", processes = [MediaType.APPLICATION_JSON])
    suspend fun post(@PathVariable supplierId: Long, @Body products: List<ProductImportDTO>): String {
        productIndexer.index(products.map {
            LOG.info("Got product ${it.supplierRef} for $supplierId")
            it.toEntity().toDoc()
        })
        return "OK"
    }
}
