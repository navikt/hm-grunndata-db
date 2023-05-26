package no.nav.hm.grunndata.db.product

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import java.util.*

@Controller("/api/v1/products")
class ProductAPIController(private val productService: ProductService) {

    @Get("/{?params*}")
    suspend fun findProducts(@QueryValue params: Map<String, String>?,
                             pageable: Pageable): Page<ProductRapidDTO> = productService.findProducts(params, pageable)

}
