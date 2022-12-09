package no.nav.hm.grunndata.db.product

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import java.util.*

@Controller("/api/v1/products")
class ProductAPIController(private val productRepository: ProductRepository) {


    @Get("/{id}")
    suspend fun getById(id: UUID): ProductDTO? =
        productRepository.findById(id)?.let {
            it.toDTO()
        }

    @Get("/")
    suspend fun listAll(pageable: Pageable): Page<ProductDTO> =
        productRepository.findAll(pageable).map { it.toDTO() }

}