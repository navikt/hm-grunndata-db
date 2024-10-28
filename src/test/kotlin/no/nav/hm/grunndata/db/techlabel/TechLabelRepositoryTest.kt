package no.nav.hm.grunndata.db.techlabel

import io.kotest.common.runBlocking
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import no.nav.hm.grunndata.db.REGISTER

@MicronautTest
class TechLabelRepositoryTest(private val techLabelRepository: TechLabelRepository) {

    @Test
    fun techLabelTest() {
        val techLabel = TechLabel(id = UUID.randomUUID(), identifier = "HMDB-20815", label = "Høyde", guide="Høyde", definition = "definisjon",
            isocode = "09070601", type = "N", unit = "cm", sort = 5, options = listOf("1", "2", "3" )
        )
        runBlocking {
            val saved = techLabelRepository.save(techLabel)
            val found = techLabelRepository.findById(saved.id)
            found.shouldNotBeNull()
            found.identifier shouldBe "HMDB-20815"
            found.label shouldBe "Høyde"
            found.guide shouldBe "Høyde"
            found.isocode shouldBe "09070601"
            found.type shouldBe "N"
            found.unit shouldBe "cm"
            found.sort shouldBe 5
            found.definition shouldBe "definisjon"
            found.options shouldBe listOf("1", "2", "3")
            val updated = techLabelRepository.update(found.copy(guide = "Høyde eller noe", updated = LocalDateTime.now()))
            updated.shouldNotBeNull()
            updated.updated shouldBeAfter saved.updated
            updated.updatedBy shouldBe REGISTER
        }
    }
}