package no.nav.hm.grunndata.db.supplier


import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository

@JdbcRepository(dialect = Dialect.POSTGRES)
interface SupplierRepository: CoroutineCrudRepository<Supplier, Long> {
    suspend fun findByIdentifier(identifier: String): Supplier?

}
