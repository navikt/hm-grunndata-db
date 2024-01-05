package no.nav.hm.grunndata.db.product

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.data.model.Pageable
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.Pagination
import no.nav.hm.grunndata.db.hmdb.product.ProductSync
import no.nav.hm.grunndata.db.fetcher
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import org.slf4j.LoggerFactory

private val LOG = LoggerFactory.getLogger(ProductSync::class.java)

@Singleton
class ProductGraphQLFetchers(
    private val productRepository: ProductRepository,
    private val bestillingsordning: Bestillingsordning,
    private val isoCategoryService: IsoCategoryService,
) {
    fun fetchers(): Map<String, DataFetcher<*>> {
        return mapOf(
            "products" to fetcher { productsFetcher(it) },
        )
    }

    private suspend fun productsFetcher(args: DataFetchingEnvironment): ProductPage {
        val pagination = Pagination.from(args)
        val page = productRepository.findAll(Pageable.from(pagination.offset!!/pagination.limit!!, pagination.limit))
        val list = page.toList().map { product ->
            val bestillingsordning = product.hmsArtNr?.let { hmsnr -> bestillingsordning.isBestillingsordning(hmsnr) }
            product.copy(
                attributes = product.attributes.copy(
                    bestillingsordning = bestillingsordning,
                )
            )
        }
        return ProductPage(page.totalSize.toInt(), list)
    }
}
