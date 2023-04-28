package no.nav.hm.grunndata.db.supplier

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import no.nav.hm.grunndata.rapid.dto.SupplierDTO
import java.time.LocalDateTime
import java.util.*

@Controller("/api/v1/suppliers")
class SupplierAPIController(private val supplierService: SupplierService) {

    @Get("/{?params*}")
    suspend fun findSuppliers(@QueryValue params: Map<String, String>?, pageable: Pageable
    ): Page<SupplierDTO> = supplierService.findAll(buildCriteriaSpec(params), pageable).map { it.toDTO() }

    private fun buildCriteriaSpec(params: Map<String, String>?): PredicateSpecification<Supplier>?
            = params?.let {
        where {
            if (params.contains("updated")) root[Supplier::updated] greaterThanOrEqualTo LocalDateTime.parse(params["updated"])
        }
    }
}
