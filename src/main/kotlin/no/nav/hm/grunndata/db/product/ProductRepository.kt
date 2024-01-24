package no.nav.hm.grunndata.db.product

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutinePageableCrudRepository
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import java.time.LocalDateTime
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
interface ProductRepository: CoroutinePageableCrudRepository<Product, UUID>, CoroutineJpaSpecificationExecutor<Product> {
    suspend fun findBySupplierIdAndSupplierRef(supplierId: UUID, supplierRef: String): Product?
    suspend fun findByIdentifier(identifier: String): Product?

    @Query("""SELECT id, identifier FROM product_v1 WHERE status = :status AND created_by = :createdBy""")
    suspend fun findIdsByStatusAndCreatedBy(status: ProductStatus, createdBy: String): List<ProductIdDTO>

    @Query("""SELECT * FROM "product_v1" WHERE agreements @> CAST(:jsonQuery AS jsonb) """, nativeQuery = true)
    suspend fun findByAgreementsJson(jsonQuery: String): List<Product>

    suspend fun findByStatusAndExpiredBefore(status: ProductStatus, expired: LocalDateTime?): List<Product>

    suspend fun findByHmsArtNr(hmsArtNr: String): Product?


}

@Introspected
data class ProductIdDTO(val id: UUID, val identifier: String)
