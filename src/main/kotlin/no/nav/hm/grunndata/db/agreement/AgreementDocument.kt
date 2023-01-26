package no.nav.hm.grunndata.db.agreement

data class AgreementDocument (
    val agreement: Agreement,
    val agreementPost: List<AgreementPost>
)

data class AgreementDocumentDTO (
    val agreement: AgreementDTO,
    val agreementPost: List<AgreementPostDTO>
)

fun AgreementDocument.toDTO(): AgreementDocumentDTO = AgreementDocumentDTO(
    agreement = agreement.toDTO(),
    agreementPost = agreementPost.map { it.toDTO() }
)


