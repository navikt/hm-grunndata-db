package no.nav.hm.grunndata.db.product

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.core.annotation.Introspected
import io.micronaut.data.model.Pageable
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.hmdb.product.ProductSync
import org.slf4j.LoggerFactory

private val LOG = LoggerFactory.getLogger(ProductSync::class.java)

private val objectMapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)

@Singleton
class ProductDataFetchers(private val productRepository: ProductRepository) {
    fun fetchers(): Map<String, DataFetcher<*>> {
        return mapOf(
            "products" to productsFetcher(),
        )
    }

    private fun productsFetcher(): DataFetcher<ProductPage> {
        return DataFetcher { args: DataFetchingEnvironment ->
            val pagination = Pagination.from(args).isSetOrDefaults()
            runBlocking {
                val page = productRepository.findAll(Pageable.from(pagination.offset!!/pagination.limit!!, pagination.limit))
                ProductPage(page.totalSize.toInt(), page.toList())
            }
        }
    }
}

@Introspected
data class Pagination (
    val offset: Int?,
    val limit: Int?,
) {
    fun isSetOrDefaults() = copy(
        offset = offset ?: 0,
        limit = limit ?: 10,
    )

    companion object {
        fun from(dataFetchingEnvironment: DataFetchingEnvironment) = objectMapper.readValue<Pagination?>(
            objectMapper.writeValueAsString(dataFetchingEnvironment.arguments["pagination"])
        ) ?: Pagination(null, null)
    }
}
