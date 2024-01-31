package no.nav.hm.grunndata.db.iso

import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

@MicronautTest
class IsoCategoryRepositoryTest(private val isoCategoryRepository: IsoCategoryRepository) {

    @Test
    fun testCrudIsoCategory() {
        val testCategory = IsoCategory(
            isoCode ="30300001",
            isoLevel = 4,
            isoTitle = "Hjelpemidler for røyking",
            isoText = "Hjelpemidler som gjør det mulig for en person å røyke. Omfatter f.eks tilpassede askebegre, lightere og sigarettholdere. Smekker og forklær, se 09 03 39",
            isoTextShort = "Røykehjelpemiddel",
            isoTranslations = IsoTranslations(titleEn = "English title", textEn = "English text"),
            isActive = true,
            showTech = true,
            allowMulti = true,
        )
        runBlocking {
            val saved = isoCategoryRepository.save(testCategory)
            saved.shouldNotBeNull()
            val read = isoCategoryRepository.findByIsoCode("30300001")
            read.shouldNotBeNull()
            read.isoLevel shouldBe testCategory.isoLevel
            read.isoTitle shouldBe testCategory.isoTitle
            read.isoText shouldBe  testCategory.isoText
            read.isoTextShort shouldBe testCategory.isoTextShort
            read.isActive shouldBe testCategory.isActive
            read.showTech shouldBe testCategory.showTech
            read.allowMulti shouldBe testCategory.allowMulti
            read.isoTranslations.titleEn shouldBe  testCategory.isoTranslations.titleEn
            read.isoTranslations.textEn shouldBe testCategory.isoTranslations.textEn
            read.updated.shouldNotBeNull()
            read.created.shouldNotBeNull()
        }

    }
}
