package no.nav.hm.grunndata.db.servicejob

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.rapid.dto.ServiceAgreementInfo
import no.nav.hm.grunndata.rapid.dto.ServiceAgreementStatus
import no.nav.hm.grunndata.rapid.dto.ServiceAttributes
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@MicronautTest
class ServiceJobRepositoryTest(private val repository: ServiceJobRepository) {

    @Test
    fun `should save and find ServiceJob by supplierId`() = runBlocking {
        val id = UUID.randomUUID()
        val supplierId = UUID.randomUUID()
        val agreementId = UUID.randomUUID()
        val job = ServiceJob(
            id = id,
            title = "Test Job",
            supplierId = supplierId,
            supplierRef = "ref-1",
            hmsArtNr = "12345",
            isoCategory = "cat-1",
            published = LocalDateTime.now(),
            expired = LocalDateTime.now().plusDays(10),
            updatedBy = "test",
            createdBy = "test",
            attributes = ServiceAttributes(),
            agreements = listOf(ServiceAgreementInfo(
                agreementId = agreementId,
                published = LocalDateTime.now(),
                expired = LocalDateTime.now().plusMonths(1),
                status = ServiceAgreementStatus.ACTIVE
            ))
        )
        repository.save(job)
        val found = repository.findBySupplierId(supplierId)
        assertTrue(found.first().supplierRef == "ref-1")
    }
}
