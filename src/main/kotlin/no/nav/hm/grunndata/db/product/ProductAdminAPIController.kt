package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import no.nav.hm.grunndata.rapid.event.EventName
import org.slf4j.LoggerFactory
import java.util.UUID

@Controller("/internal/v1/products")
class ProductAdminAPIController(
    private val productService: ProductService,
    private val attributeTagService: AttributeTagService,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProductAdminAPIController::class.java)
    }

    @Post("/{id}/fix-product-attributes")
    suspend fun fixProductAttributesFor(id: UUID): FixProductAttributesResponse {
        val product = productService.findByIdDTO(id) ?: return FixProductAttributesResponse(error = "product not found")
        val dto = productService.saveAndPushTokafka(product.toEntity(), EventName.syncedRegisterProductV1)
        return FixProductAttributesResponse(dto)
    }

    @Post("/look-for-invalid-product-attributes")
    suspend fun lookForInvalidProductAttributes(@Body req: LookForInvalidProductAttributesRequest): LookForInvalidProductAttributesResponse {
        return runCatching {
            val page = productService.findProducts(null, Pageable.from(req.page ?: 0, req.size ?: 5, Sort.of(Sort.Order.asc("id"))))
            val mismatches = mutableListOf<LookForInvalidProductAttributesItemResponse>()
            page.forEach { prodDto ->
                val prod = prodDto.toEntity()
                val orig = objectMapper.writeValueAsString(prod)
                val enrichedProdEntity = listOf(
                    attributeTagService::addBestillingsordningAttribute,
                    attributeTagService::addDigitalSoknadAttribute,
                    attributeTagService::addSortimentKategoriAttribute,
                    attributeTagService::addPakrevdGodkjenningskursAttribute,
                    attributeTagService::addProdukttypeAttribute,
                ).fold(prod) { it, enricher -> enricher.call(it) }
                val enriched = objectMapper.writeValueAsString(enrichedProdEntity)
                if (enriched != orig) {
                    mismatches.add(LookForInvalidProductAttributesItemResponse(
                        id = prod.id,
                        hmsnr = prod.hmsArtNr ?: "<none>",
                        urlDev = "https://finnhjelpemiddel.intern.dev.nav.no/produkt/${prod.seriesUUID}",
                        urlProd = "https://finnhjelpemiddel.nav.no/produkt/${prod.seriesUUID}",
                    ))
                    if (req.debugLog == true) LOG.info("DEBUG: Mismatch found: orig=${orig}, enriched=${enriched}")
                    if (req.update == true) {
                        LOG.info("Updating stale product attributes for id=${prod.id}")
                        productService.saveAndPushTokafka(prod, EventName.syncedRegisterProductV1)
                    }
                } else {
                    if (req.debugLog == true) LOG.info("DEBUG: id=${prod.id} matched")
                }
            }
            LookForInvalidProductAttributesResponse(
                totalPages = page.totalPages,
                totalSize = page.totalSize,
                mismatches = mismatches,
            )
        }.getOrElse { e ->
            LOG.error("Exception thrown", e)
            LookForInvalidProductAttributesResponse(
                error = "Exception: $e",
            )
        }
    }
}

data class FixProductAttributesResponse(
    val dto: ProductRapidDTO? = null,
    val error: String? = null,
)

data class LookForInvalidProductAttributesRequest(
    val page: Int? = null,
    val size: Int? = null,
    val update: Boolean? = null,
    val debugLog: Boolean? = null,
)

data class LookForInvalidProductAttributesResponse(
    val totalPages: Int? = null,
    val totalSize: Long? = null,
    val mismatches: List<LookForInvalidProductAttributesItemResponse>? = null,
    val error: String? = null,
)

data class LookForInvalidProductAttributesItemResponse(
    val id: UUID,
    val hmsnr: String,
    val urlDev: String,
    val urlProd: String,
)
