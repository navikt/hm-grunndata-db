package no.nav.hm.grunndata.db.supplier


import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository

@JdbcRepository(dialect = Dialect.POSTGRES)
interface SupplierRepository: CoroutineCrudRepository<Supplier, Long> {
    /**
     * For supplier from legacy database.
     */
    @Query("INSERT INTO $SupplierTableName(id, hmdb_id, uuid, name, info, created, updated ) VALUES (:id, :hmdbId, :uuid, :name, :info::jsonb, :created, :updated)")
    fun insertLegacy(supplier: Supplier)

}
