package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository

@JdbcRepository(dialect = Dialect.POSTGRES)
interface HmDbBatchRepository: CrudRepository<HmDbBatch, Long> {

    fun findByName(name: String): HmDbBatch?

}