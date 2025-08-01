package no.nav.hm.grunndata.db.index.item

import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDateTime
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface IndexItemRepository: CoroutineCrudRepository<IndexItem, UUID>, CoroutineJpaSpecificationExecutor<IndexItem> {

    suspend fun findByOid(oid: String): IndexItem?
    suspend fun findByIndexType(indexType: IndexType): List<IndexItem>

    @Query(
        value = "SELECT * FROM index_item WHERE status = :status ORDER BY updated ASC LIMIT :limit FOR UPDATE SKIP LOCKED",
        nativeQuery = true
    )
    suspend fun findAndLockForProcessing(status: IndexItemStatus, limit: Int): List<IndexItem>

    suspend fun findByStatusAndUpdatedBefore(
        status: IndexItemStatus,
        updated: LocalDateTime,
        pageable: Pageable
    ): List<IndexItem>

    suspend fun deleteByStatusAndUpdatedBefore(
        status: IndexItemStatus,
        updated: LocalDateTime
    ): Long?
}