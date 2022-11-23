package no.nav.hm.grunndata.db.agreement

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository

@JdbcRepository(dialect = Dialect.POSTGRES)
@CacheConfig("agreements")
interface AgreementRepository: CoroutineCrudRepository<Agreement,Long> {
    @Cacheable
    suspend fun findByIdentifier(identifier: String): Agreement?

}