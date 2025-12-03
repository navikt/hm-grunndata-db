package no.nav.hm.grunndata.db.servicejob

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutinePageableCrudRepository
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
interface ServiceJobRepository : CoroutinePageableCrudRepository<ServiceJob, UUID>, CoroutineJpaSpecificationExecutor<ServiceJob> {
    suspend fun findBySupplierId(supplierId: UUID): List<ServiceJob>
}
