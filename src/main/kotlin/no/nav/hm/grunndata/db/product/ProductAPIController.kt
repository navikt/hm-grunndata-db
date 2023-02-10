package no.nav.hm.grunndata.db.product

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import no.nav.hm.grunndata.dto.ProductDTO
import java.util.*

@Controller
class ProductAPIController(private val productService: ProductService) {

    @Get("/{?params*}")
    suspend fun findProducts(@QueryValue params: HashMap<String, String>?,
                             pageable: Pageable): Page<ProductDTO> = productService.findProducts(params, pageable)

}