package no.nav.hm.grunndata.db.agreement

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import no.nav.hm.grunndata.rapid.dto.AgreementStatus
import java.time.LocalDateTime
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
interface AgreementRepository: CoroutineCrudRepository<Agreement, UUID>, CoroutineJpaSpecificationExecutor<Agreement> {
    suspend fun findByIdentifier(identifier: String): Agreement?
    suspend fun findByStatusAndExpiredBefore(status: AgreementStatus, expired: LocalDateTime? = LocalDateTime.now()): List<Agreement>

    @Query("""SELECT id, identifier FROM agreement_v1 WHERE status = :status""")
    suspend fun findIdsByStatus(status: AgreementStatus): List<AgreementIdDTO>

}

@Introspected
data class AgreementIdDTO(val id: UUID, val identifier: String)