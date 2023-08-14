package no.nav.hm.grunndata.db.techlabel

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
interface TechLabelRepository: CoroutineCrudRepository<TechLabel, UUID>, CoroutineJpaSpecificationExecutor<TechLabel> {

    fun findByIdentifier(identifier: String): TechLabel?

}