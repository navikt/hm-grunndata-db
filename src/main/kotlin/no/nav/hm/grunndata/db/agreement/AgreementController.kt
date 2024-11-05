package no.nav.hm.grunndata.db.agreement

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.where
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.annotation.RequestBean
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.rapid.dto.AgreementDTO
import java.time.LocalDateTime
import java.util.*
import org.slf4j.LoggerFactory

@Controller("/api/v1/agreements")
class AgreementDocumentController(private val agreementService: AgreementService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementDocumentController::class.java)
    }

    @Get("/{id}")
    suspend fun getAgreementDocument(id:UUID): AgreementDTO? = agreementService.findById(id)?.let {
                it.toDTO() }


    @Get("/")
    suspend fun findAgreements(@RequestBean criteria: AgreementCriteria, pageable: Pageable
    ): Page<AgreementDTO> {
        return agreementService.findAll(buildCriteriaSpec(criteria), pageable).map { it.toDTO() }
    }

    private fun buildCriteriaSpec(crit: AgreementCriteria): PredicateSpecification<Agreement>? =
      if (crit.isNotEmpty()) {
             where {
                crit.reference?.let { root[Agreement::reference] eq it }
                crit.updatedAfter?.let { root[Agreement::updated] greaterThanOrEqualTo it }
                crit.status?.let { root[Agreement::status] eq it }
                crit.expiredAfter?.let { root[Agreement::expired] greaterThanOrEqualTo it }
            }
        } else null

}
@Introspected
data class AgreementCriteria (
    val reference: String?,
    val updatedAfter: LocalDateTime?,
    val status: String?,
    val expiredAfter: LocalDateTime?
) {
    fun isNotEmpty(): Boolean = reference != null || updatedAfter != null || status != null || expiredAfter != null
}
