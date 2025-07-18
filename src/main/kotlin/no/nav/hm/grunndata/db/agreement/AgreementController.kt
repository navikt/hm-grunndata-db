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

    @Get("/{id}")
    suspend fun getAgreementDocument(id:UUID): AgreementDTO? = agreementService.findById(id)?.let {
                it.toDTO() }


    @Get("/")
    suspend fun findAgreements(@RequestBean criteria: AgreementCriteria, pageable: Pageable
    ): Page<AgreementDTO> {
        return agreementService.findAll(criteria, pageable).map { it.toDTO() }
    }

}

@Introspected
data class AgreementCriteria (
    val reference: String? = null,
    val updatedAfter: LocalDateTime? = null,
    val status: String? = null,
    val expiredAfter: LocalDateTime? = null
) {
    fun isNotEmpty(): Boolean = reference != null || updatedAfter != null || status != null || expiredAfter != null
}
