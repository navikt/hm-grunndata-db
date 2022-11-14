package no.nav.hm.grunndata.db.agreement

data class AgreementDocument (
    val agreement: Agreement,
    val agreementPost: List<AgreementPost>
)