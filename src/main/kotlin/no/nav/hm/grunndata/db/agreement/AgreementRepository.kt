package no.nav.hm.grunndata.db.agreement

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
@CacheConfig("agreements")
interface AgreementRepository: CoroutineCrudRepository<Agreement, UUID> {
    @Cacheable
    suspend fun findByIdentifier(identifier: String): Agreement?

    suspend fun findAgreements(params: HashMap<String, String>?, pageable: Pageable): Page<Agreement>

}