package no.nav.hm.grunndata.db.series

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutinePageableCrudRepository
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
interface SeriesRepository: CoroutinePageableCrudRepository<Series, UUID>, CoroutineJpaSpecificationExecutor<Series> {

    suspend fun findByIdentifier(identifier: String): Series?


}