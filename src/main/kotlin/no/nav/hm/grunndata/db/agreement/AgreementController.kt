package no.nav.hm.grunndata.db.agreement

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.product.Product
import no.nav.hm.grunndata.dto.AgreementDTO
import no.nav.hm.grunndata.dto.ProductDTO
import java.time.LocalDateTime
import java.util.HashMap
import java.util.UUID

@Controller("/api/v1/agreements")
class AgreementDocumentController(private val agreementRepository: AgreementRepository) {

    @Get("/{id}")
    fun getAgreementDocument(id:UUID): AgreementDTO? = runBlocking {
        agreementRepository.findById(id)?.let {
                it.toDTO()
        }
    }

    @Get("/{?params*}")
    suspend fun findAgreements(@QueryValue params: Map<String, String>?, pageable: Pageable
    ): Page<AgreementDTO> = agreementRepository.findAll(buildCriteriaSpec(params), pageable).map { it.toDTO() }



    private fun buildCriteriaSpec(params: Map<String, String>?): PredicateSpecification<Agreement>?
            = params?.let {
        where {
            if (params.contains("reference")) root[Agreement::reference] eq params["reference"]
            if (params.contains("updated")) root[Agreement::updated] greaterThanOrEqualTo LocalDateTime.parse(params["updated"])
        }
    }

}