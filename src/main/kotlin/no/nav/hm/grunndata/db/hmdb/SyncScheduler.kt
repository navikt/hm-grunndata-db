package no.nav.hm.grunndata.db.hmdb

import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Singleton
class SyncScheduler(private val hmDbClient: HmDbClient, private val hmdbBatchRepository: HmDbBatchRepository,
                    private val supplierRepository: SupplierRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SyncScheduler::class.java)
    }

    @Scheduled(cron = "0 30 * * * *")
    fun syncSuppliers() {
        LOG.info("Running sync suppliers")
        val syncSupplier = hmdbBatchRepository.findByName(SYNC_SUPPLIERS) ?:
            hmdbBatchRepository.save(HmDbBatch(name= SYNC_SUPPLIERS,
                syncfrom = LocalDateTime.now().minusYears(20).truncatedTo(ChronoUnit.SECONDS)))
        val suppliers = hmDbClient.fetchSuppliers(syncSupplier.syncfrom)
        val entities = suppliers.map { it.toSupplier() }
        runBlocking {
            entities.forEach {
                val supplier = supplierRepository.findByIdentifier(it.identifier)

            }
        }

    }
}