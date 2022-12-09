package no.nav.hm.grunndata.db.product

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutinePageableCrudRepository
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
interface ProductRepository: CoroutinePageableCrudRepository<Product, UUID> {
    suspend fun findBySupplierIdAndSupplierRef(supplierId: UUID, supplierRef: String): Product?
    suspend fun findByIdentifier(identifier: String): Product?
}
