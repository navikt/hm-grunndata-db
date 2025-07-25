package no.nav.hm.grunndata.db.index.item

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface IndexItemRepository: CoroutineCrudRepository<IndexItem, UUID>, CoroutineJpaSpecificationExecutor<IndexItem> {

    suspend fun findByOid(oid: String): IndexItem?
    suspend fun findByIndexType(indexType: IndexType): List<IndexItem>
    suspend fun findByStatusOrderByUpdatedAsc(status: IndexItemStatus, pageable: Pageable): List<IndexItem>

}