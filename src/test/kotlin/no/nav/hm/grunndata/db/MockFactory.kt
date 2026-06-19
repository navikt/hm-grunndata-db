package no.nav.hm.grunndata.db

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.mockk.coEvery
import io.mockk.mockk
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.db.techlabel.TechLabelService
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.opensearch.client.opensearch.OpenSearchClient


@Factory
class MockFactory {

    companion object {
        val sourceHmsNr = "123456789"
        val targetHmsNr = "987654321"

    }
    @Singleton
    @Replaces(bean = RapidPushService::class)
    fun rapidPushService(): RapidPushService = mockk(relaxed = true)

    @Singleton
    @Replaces(bean = IsoCategoryService::class)
    fun mockIsoCategoryService(): IsoCategoryService = mockk<IsoCategoryService>(relaxed = true).apply {
        coEvery { lookUpCode("123456") } returns
                IsoCategoryDTO(
                    isoCode = "123456",
                    isoTitle = "mock-title",
                    isoText = "mock-text",
                    isoLevel = 4
                )
    }

    @Singleton
    @Replaces(bean = OpenSearchClient::class)
    fun mockOpenSearchClient(): OpenSearchClient = mockk<OpenSearchClient>(relaxed = true)

    @Singleton
    @Replaces(bean = OpensearchIndexer::class)
    fun mockOpensearchIndexer(): OpensearchIndexer = mockk<OpensearchIndexer>(relaxed = true)

    @Singleton
    @Replaces(bean = TechLabelService::class)
    fun mockTechLabelService(): TechLabelService = mockk(relaxed = true)

}