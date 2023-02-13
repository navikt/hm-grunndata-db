package no.nav.hm.grunndata.db.supplier


import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.util.*


@JdbcRepository(dialect = Dialect.POSTGRES)
@CacheConfig("suppliers")
interface SupplierRepository: CoroutineCrudRepository<Supplier, UUID>, CoroutineJpaSpecificationExecutor<Supplier> {

    @Cacheable
    suspend fun findByIdentifier(identifier: String): Supplier?

    @Cacheable
    override suspend fun findById(id: UUID): Supplier?

}
