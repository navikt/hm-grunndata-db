package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.repository.kotlin.CoroutinePageableCrudRepository

@JdbcRepository(dialect = Dialect.POSTGRES)
interface HmDbBatchRepository: CoroutinePageableCrudRepository<HmDbBatch, Long> {

    suspend fun findByName(name: String): HmDbBatch?

}