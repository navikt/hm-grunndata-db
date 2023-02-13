package no.nav.hm.grunndata.db.agreement

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.dto.AgreementDTO
import no.nav.hm.grunndata.dto.ProductDTO
import java.util.HashMap
import java.util.UUID

@Controller("/api/v1/agreement")
class AgreementDocumentController(private val agreementRepository: AgreementRepository) {

    @Get("/{id}")
    fun getAgreementDocument(id:UUID): AgreementDTO? = runBlocking {
        agreementRepository.findById(id)?.let {
                it.toDTO()
        }
    }

    @Get("/{?params*}")
    suspend fun findAgreements(@QueryValue params: HashMap<String, String>?, pageable: Pageable
    ): Page<AgreementDTO> = agreementRepository.findAgreements(params, pageable).map { it.toDTO() }

}