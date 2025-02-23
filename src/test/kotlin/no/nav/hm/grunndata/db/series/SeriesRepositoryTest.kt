package no.nav.hm.grunndata.db.series

import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.rapid.dto.MediaInfo
import no.nav.hm.grunndata.rapid.dto.SeriesData
import org.junit.jupiter.api.Test
import java.util.*
import no.nav.hm.grunndata.db.REGISTER

@MicronautTest
class SeriesRepositoryTest(private val seriesRepository: SeriesRepository) {

    @Test
    fun crudSeries() {
        val supplierId = UUID.randomUUID()
        val series = Series (
            identifier = "HMDB-12345",
            title = "en test series 1",
            text = "en test series 1 beskrivelse",
            isoCategory = "12001314",
            seriesData = SeriesData(media = setOf(MediaInfo(sourceUri = "http://example.com", uri = "http://example.com"))),
            supplierId = supplierId,
            createdBy = REGISTER,
            updatedBy = REGISTER )
        runBlocking {
            val saved = seriesRepository.save(series)
            val found = seriesRepository.findById(series.id)
            found.shouldNotBeNull()
            val identifier = seriesRepository.findByIdentifier(found.identifier)
            identifier.shouldNotBeNull()
            identifier.title shouldBe series.title
            val updated = seriesRepository.update(identifier.copy(title = "en test series 2"))
            updated.isoCategory shouldBe "12001314"
            updated.seriesData.shouldNotBeNull()
            updated.seriesData!!.media.size shouldBe 1
        }
    }
}