package no.nav.hm.grunndata.db.agreement

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import kotlinx.coroutines.runBlocking
import java.util.UUID

@Controller("/api/v1/agreement")
class AgreementDocumentController(private val agreementRepository: AgreementRepository) {

    @Get("/{id}")
    fun getAgreementDocument(id:UUID): AgreementDTO? = runBlocking {
        agreementRepository.findById(id)?.let {
                it.toDTO()
        }
    }

}