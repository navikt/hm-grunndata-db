package no.nav.hm.grunndata.db.supplier

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import no.nav.hm.grunndata.dto.SupplierDTO
import java.util.HashMap

@Controller
class SupplierAPIController(private val supplierRepository: SupplierRepository) {

    @Get("/{?params*}")
    suspend fun findSuppliers(@QueryValue params: HashMap<String, String>?, pageable: Pageable
    ): Page<SupplierDTO> = supplierRepository.findSuppliers(params, pageable).map { it.toDTO() }
}