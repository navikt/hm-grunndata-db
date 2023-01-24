package no.nav.hm.grunndata.db.agreement

import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
open class AgreementDocumentService(private val agreementRepository: AgreementRepository,
                               private val agreementPostRepository: AgreementPostRepository) {

    @Transactional
    open suspend fun saveAgreementDocument(doc: AgreementDocument): AgreementDocument =
        agreementRepository.findByIdentifier(doc.agreement.identifier)?.let { agree ->
            updateAgreement(doc, agree)
        } ?: saveAgreement(doc)



    private suspend fun updateAgreement(agreementDocument: AgreementDocument, agree: Agreement) : AgreementDocument =
        AgreementDocument(agreement = agreementRepository.update(agreementDocument.agreement.copy(id = agree.id, created = agree.created)),
            agreementPost = agreementDocument.agreementPost.map { post -> agreementPostRepository.findByIdentifier(post.identifier)?.let { db ->
                agreementPostRepository.update(post.copy(id = db.id, agreementId = agree.id, created = db.created))
            } ?: agreementPostRepository.save(post.copy(agreementId = agree.id))} )


    private suspend fun saveAgreement(agreementDocument: AgreementDocument) : AgreementDocument =
        AgreementDocument(agreement = agreementRepository.save(agreementDocument.agreement),
        agreementPost = agreementDocument.agreementPost.map { post ->
            agreementPostRepository.save(post.copy(agreementId = agreementDocument.agreement.id))
        })

}