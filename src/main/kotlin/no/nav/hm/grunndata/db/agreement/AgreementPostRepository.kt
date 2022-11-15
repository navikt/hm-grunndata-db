package no.nav.hm.grunndata.db.agreement

import io.micronaut.data.repository.kotlin.CoroutineCrudRepository

interface AgreementPostRepository: CoroutineCrudRepository<AgreementPost, Long> {
    suspend fun findByAgreementId(agreementId: Long): List<AgreementPost>
    suspend fun findByIdentifier(identifier: String): AgreementPost?
}
