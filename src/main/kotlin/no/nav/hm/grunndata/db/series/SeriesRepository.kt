package no.nav.hm.grunndata.db.series

import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutinePageableCrudRepository
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
interface SeriesRepository: CoroutinePageableCrudRepository<Series, UUID>, CoroutineJpaSpecificationExecutor<Series> {

    suspend fun findByIdentifier(identifier: String): Series?

    suspend fun findBySupplierId(supplierId: UUID): List<Series>

    @Query("SELECT series_identifier FROM product_v1 WHERE status='DELETED' AND NOT EXISTS (SELECT FROM series_v1 WHERE series_identifier = identifier)")
    suspend fun findDeletedSeriesThatDoesNotExist(): List<String>

}