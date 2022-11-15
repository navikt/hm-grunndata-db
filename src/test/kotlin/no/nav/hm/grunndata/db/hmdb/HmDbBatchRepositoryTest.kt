package no.nav.hm.grunndata.db.hmdb

import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@MicronautTest
class HmDbBatchRepositoryTest(private val repository: HmDbBatchRepository) {

    @Test
    fun testHmDbBatchRepository() {
        val notFound = repository.findByName(SYNC_AGREEMENTS)
        notFound.shouldBeNull()
        val syncNews = HmDbBatch(name= SYNC_AGREEMENTS,
            syncfrom = LocalDateTime.now().minusMonths(6).truncatedTo(ChronoUnit.SECONDS))
        val saved = repository.save(syncNews)
        saved.id shouldBeGreaterThan -1
        val found = repository.findByName(SYNC_AGREEMENTS)
        found.shouldNotBeNull()
        val updated = repository.update(found.copy(syncfrom = LocalDateTime.now().minusMonths(3).truncatedTo(ChronoUnit.SECONDS)))
        updated.syncfrom shouldBeAfter saved.syncfrom
    }
}