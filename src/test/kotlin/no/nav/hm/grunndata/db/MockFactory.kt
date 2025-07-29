package no.nav.hm.grunndata.db

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.mockk.coEvery
import io.mockk.mockk
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.iso.IsoCategoryService
import no.nav.hm.grunndata.rapid.dto.Attributes
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO
import no.nav.hm.grunndata.rapid.dto.MediaInfo
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.dto.SupplierDTO
import no.nav.hm.grunndata.rapid.dto.SupplierInfo

import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import java.time.LocalDateTime
import java.util.UUID
import kotlin.apply


@Factory
class MockFactory {

    companion object {
        val sourceHmsNr = "123456789"
        val targetHmsNr = "987654321"

    }
    @Singleton
    @Replaces
    fun rapidPushService(): RapidPushService = mockk(relaxed = true)

    @Singleton
    @Replaces
    fun mockIsoCategoryService(): IsoCategoryService = mockk<IsoCategoryService>(relaxed = true).apply {
        coEvery { lookUpCode("123456") } returns
                IsoCategoryDTO(
                    isoCode = "123456",
                    isoTitle = "mock-title",
                    isoText = "mock-text",
                    isoLevel = 4
                )
    }

}