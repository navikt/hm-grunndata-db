package no.nav.hm.grunndata.db.agreement

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import kotlinx.coroutines.runBlocking
import java.util.UUID

@Controller("/api/v1/agreementdocument")
class AgreementDocumentController(private val agreementRepository: AgreementRepository,
                                  private val agreementPostRepository: AgreementPostRepository) {

    @Get("/{id}")
    fun getAgreementDocument(id:UUID): AgreementDocument? = runBlocking {
            agreementRepository.findById(id)?.let {
                AgreementDocument(it, agreementPostRepository.findByAgreementId(id))
            }
    }

}