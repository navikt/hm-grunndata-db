package no.nav.hm.grunndata.db.iso

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.fetcher
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO

@Singleton
class IsoCategoryGraphQLFetchers(
    private val isoCategoryService: IsoCategoryService,
) {
    fun fetchers(): Map<String, DataFetcher<*>> {
        return mapOf(
            "isoCategories" to fetcher { isoCategoriesFetcher() },
            "isoCategory" to fetcher { isoCategoryFetcher(it) },
        )
    }

    private fun isoCategoriesFetcher(): List<IsoCategoryDTO> {
        return isoCategoryService.retrieveAllCategories()
    }

    private fun isoCategoryFetcher(args: DataFetchingEnvironment): IsoCategoryDTO? {
        val isoCategory: String = args.arguments["isoCategory"]?.toString() ?: return null
        return isoCategoryService.lookUpCode(isoCategory)
    }
}
