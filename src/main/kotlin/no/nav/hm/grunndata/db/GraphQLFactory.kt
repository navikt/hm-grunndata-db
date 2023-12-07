package no.nav.hm.grunndata.db

import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.core.io.ResourceResolver
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.product.ProductDataFetcher
import java.io.BufferedReader
import java.io.InputStreamReader

@Factory
class GraphQLFactory {
    @Bean
    @Singleton
    fun graphQL(resourceResolver: ResourceResolver, productDataFetcher: ProductDataFetcher): GraphQL {
        val schemaParser = SchemaParser()
        val schemaGenerator = SchemaGenerator()

        // Parse the schema.
        val typeRegistry = TypeDefinitionRegistry();
        typeRegistry.merge(schemaParser.parse(BufferedReader(InputStreamReader(
            resourceResolver.getResourceAsStream("classpath:schema.graphqls").get()))))

        // Create the runtime wiring.
        val runtimeWiring = RuntimeWiring.newRuntimeWiring()
            .type("Query") { typeWiring -> typeWiring
                .dataFetcher("hello", productDataFetcher) }
            .build()

        // Create the executable schema.
        val graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring)

        // Return the GraphQL bean.
        return GraphQL.newGraphQL(graphQLSchema).build()
    }
}
