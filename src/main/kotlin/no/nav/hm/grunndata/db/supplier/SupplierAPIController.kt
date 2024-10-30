package no.nav.hm.grunndata.db.supplier

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.annotation.RequestBean
import no.nav.hm.grunndata.rapid.dto.SupplierDTO
import java.time.LocalDateTime
import java.util.*
import no.nav.hm.grunndata.rapid.dto.SupplierStatus

@Controller("/api/v1/suppliers")
class SupplierAPIController(private val supplierService: SupplierService) {

    @Get("/")
    suspend fun findSuppliers(
        @RequestBean supplierCriteria: SupplierCriteria, pageable: Pageable
    ): Page<SupplierDTO> = supplierService.findSuppliers(buildCriteriaSpec(supplierCriteria), pageable)

    @Get("/{supplierId}")
    suspend fun findById(supplierId: UUID) = supplierService.findByIdDTO(supplierId)

    private fun buildCriteriaSpec(crit: SupplierCriteria): PredicateSpecification<Supplier>? =
        if (crit.isNotEmpty()) {
            where {
                crit.updated?.let { root[Supplier::updated] greaterThanOrEqualTo it }
                crit.status?.let { root[Supplier::status] eq it }
                crit.createdBy?.let { root[Supplier::createdBy] eq it }
            }
        } else null

    data class SupplierCriteria(
        val updated: LocalDateTime?,
        val status: SupplierStatus?,
        val createdBy: String?
    ) {
        fun isNotEmpty(): Boolean = updated != null || status != null || createdBy != null
    }
}
