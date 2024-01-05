package no.nav.hm.grunndata.db

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import graphql.GraphQL
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.io.ResourceResolver
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.iso.IsoCategoryGraphQLFetchers
import no.nav.hm.grunndata.db.product.ProductGraphQLFetchers
import java.io.BufferedReader
import java.io.InputStreamReader

@Factory
class GraphQLFactory {
    @Bean
    @Singleton
    fun graphQL(
        resourceResolver: ResourceResolver,
        isoCategoryGraphQLFetchers: IsoCategoryGraphQLFetchers,
        productGraphQLFetchers: ProductGraphQLFetchers,
    ): GraphQL {
        val schemaParser = SchemaParser()
        val schemaGenerator = SchemaGenerator()

        // Parse the schema.
        val typeRegistry = TypeDefinitionRegistry();
        typeRegistry.merge(schemaParser.parse(BufferedReader(InputStreamReader(
            resourceResolver.getResourceAsStream("classpath:schema.graphqls").get()))))

        // Create the runtime wiring.
        val runtimeWiring = RuntimeWiring.newRuntimeWiring()
            .type("Query") { typeWiring -> typeWiring
                .dataFetchers(isoCategoryGraphQLFetchers.fetchers())
                .dataFetchers(productGraphQLFetchers.fetchers())
            }
            .build()

        // Create the executable schema.
        val graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring)

        // Return the GraphQL bean.
        return GraphQL.newGraphQL(graphQLSchema).build()
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
        fun from(args: DataFetchingEnvironment) = (
            args.getArgumentAs("pagination") ?: Pagination(null, null)
        ).isSetOrDefaults()
    }
}

fun <R> fetcher(block: suspend (args: DataFetchingEnvironment) -> R): DataFetcher<R> = DataFetcher { runBlocking(Dispatchers.IO) { block(it) } }

// Used to convert from java hashmaps into kotlin data types
val objectConverter = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

inline fun <T: DataFetchingEnvironment, reified R> T.getArgumentAs(arg: String): R? {
    return arguments[arg]?.let { objectConverter.convertValue<R>(it) }
}
