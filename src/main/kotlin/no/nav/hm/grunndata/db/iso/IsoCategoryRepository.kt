package no.nav.hm.grunndata.db.iso

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository


@JdbcRepository(dialect = Dialect.POSTGRES)
interface IsoCategoryRepository: CoroutineCrudRepository<IsoCategory, String>
