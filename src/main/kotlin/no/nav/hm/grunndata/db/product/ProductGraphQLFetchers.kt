package no.nav.hm.grunndata.db.product

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.data.model.Pageable
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.Pagination
import no.nav.hm.grunndata.db.hmdb.product.ProductSync
import no.nav.hm.grunndata.db.fetcher
import no.nav.hm.grunndata.db.getArgumentAs
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import org.slf4j.LoggerFactory

private val LOG = LoggerFactory.getLogger(ProductSync::class.java)

