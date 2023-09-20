package no.nav.hm.grunndata.db.series

import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.db.HMDB
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class SeriesRepositoryTest(private val seriesRepository: SeriesRepository) {

    @Test
    fun crudSeries() {
        val supplierId = UUID.randomUUID()
        val series = Series (
            identifier = "HMDB-12345",
            name = "en test series 1",
            supplierId = supplierId,
            createdBy = HMDB,
            updatedBy = HMDB )
        runBlocking {
            val saved = seriesRepository.save(series)
            val found = seriesRepository.findById(series.id)
            found.shouldNotBeNull()
            val identifier = seriesRepository.findByIdentifier(found.identifier)
            identifier.shouldNotBeNull()
            identifier.name shouldBe series.name
            val updated = seriesRepository.update(identifier.copy(name = "en test series 2"))
        }
    }
}