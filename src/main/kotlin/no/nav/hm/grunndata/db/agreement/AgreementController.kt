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
import no.nav.hm.grunndata.rapid.dto.AgreementDTO
import java.time.LocalDateTime
import java.util.*

@Controller("/api/v1/agreements")
class AgreementDocumentController(private val agreementService: AgreementService) {

    @Get("/{id}")
    suspend fun getAgreementDocument(id:UUID): AgreementDTO? = agreementService.findById(id)?.let {
                it.toDTO()
        }


    @Get("/{?params*}")
    suspend fun findAgreements(@QueryValue params: Map<String, String>?, pageable: Pageable
    ): Page<AgreementDTO> = agreementService.findAll(buildCriteriaSpec(params), pageable).map { it.toDTO() }



    private fun buildCriteriaSpec(params: Map<String, String>?): PredicateSpecification<Agreement>?
            = params?.let {
        where {
            if (params.contains("reference")) root[Agreement::reference] eq params["reference"]
            if (params.contains("updatedAfter")) root[Agreement::updated] greaterThanOrEqualTo LocalDateTime.parse(params["updatedAfter"])
            if (params.contains("status")) root[Agreement::status] eq params["status"]
            if (params.contains("expiredAfter")) root[Agreement::expired] greaterThanOrEqualTo LocalDateTime.parse(params["expiredAfter"])
        }
    }

}
