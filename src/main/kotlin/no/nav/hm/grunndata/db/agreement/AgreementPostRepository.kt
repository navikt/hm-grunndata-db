package no.nav.hm.grunndata.db.agreement

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository

@JdbcRepository(dialect = Dialect.POSTGRES)
interface AgreementPostRepository: CoroutineCrudRepository<AgreementPost, Long> {
    suspend fun findByAgreementId(agreementId: Long): List<AgreementPost>
    suspend fun findByIdentifier(identifier: String): AgreementPost?
}
