package no.nav.hm.grunndata.db.product

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import no.nav.hm.grunndata.rapid.event.EventName
import java.util.UUID

@Controller("/internal/v1/products")
class ProductAdminAPIController(private val productService: ProductService) {
    @Post("/{id}/fix-product-attributes")
    suspend fun fixProductAttributesFor(id: UUID): Response {
        val product = productService.findByIdDTO(id) ?: return Response(error = "product not found")
        val dto = productService.saveAndPushTokafka(product.toEntity(), EventName.syncedRegisterProductV1)
        return Response(dto)
    }
}

data class Response(
    val dto: ProductRapidDTO? = null,
    val error: String? = null,
)
