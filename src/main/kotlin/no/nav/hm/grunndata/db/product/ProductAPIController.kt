package no.nav.hm.grunndata.db.product

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.RequestBean
import java.time.LocalDateTime
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import java.util.*

@Controller("/api/v1/products")
class ProductAPIController(private val productService: ProductService) {

    @Get("/")
    suspend fun findProducts(@RequestBean productCriteria: ProductCriteria, pageable: Pageable): Page<ProductRapidDTO> =
        productService.findProducts(buildCriteriaSpec(productCriteria), pageable)

    private fun buildCriteriaSpec(criteria: ProductCriteria): PredicateSpecification<Product>? =
        if (criteria.isNotEmpty()) {
            where {
                criteria.supplierRef?.let { root[Product::supplierRef] eq it }
                criteria.supplierId?.let { root[Product::supplierId] eq it }
                criteria.updated?.let { root[Product::updated] greaterThanOrEqualTo it }
                criteria.status?.let { root[Product::status] eq it }
                criteria.seriesUUID?.let { root[Product::seriesUUID] eq it }
                criteria.isoCategory?.let { root[Product::isoCategory] eq it }
                criteria.accessory?.let { root[Product::accessory] eq it }
                criteria.sparePart?.let { root[Product::sparePart] eq it }
            }
        } else null


    @Get("/{id}")
    suspend fun findById(id: UUID): ProductRapidDTO? = productService.findByIdDTO(id)

    @Get("/{supplierId}/{supplierRef}")
    suspend fun findBySupplierIdAndSupplierRef(supplierId: UUID, supplierRef: String) =
        productService.findBySupplierIdAndSupplierRef(supplierId, supplierRef)

    @Get("/isoCategory/thatHasHmsnr")
    suspend fun findDistinctIsoCategoryThatHasHmsnr(): Set<String> = productService.findDistinctIsoCategoryThatHasHmsnr()

    @Get("/hmsArtNr/{hmsArtNr}")
    suspend fun findByHmsArtNr(hmsArtNr: String): ProductRapidDTO? = productService.findByHmsArtNr(hmsArtNr)

}

@Introspected
data class ProductCriteria(
    val supplierRef: String?,
    val supplierId: UUID?,
    val updated: LocalDateTime?,
    val status: String?,
    val seriesUUID: UUID?,
    val isoCategory: String?,
    val accessory: Boolean?,
    val sparePart: Boolean?,
) {
    fun isNotEmpty(): Boolean =
        supplierRef != null || supplierId != null || updated != null || status != null || seriesUUID != null
                || isoCategory != null || accessory != null || sparePart != null

}
