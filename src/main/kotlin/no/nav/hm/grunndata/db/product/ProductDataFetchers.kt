package no.nav.hm.grunndata.db.product

import graphql.schema.DataFetcher
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

@Singleton
class ProductDataFetchers(private val productRepository: ProductRepository) {
    fun productsFetcher(): DataFetcher<List<Product>> {
        return DataFetcher { /*dataFetchingEnvironment: DataFetchingEnvironment*/ _ ->
            runBlocking {
                productRepository
                    .findAll()
                    .toList()
            }
        }
    }
}
