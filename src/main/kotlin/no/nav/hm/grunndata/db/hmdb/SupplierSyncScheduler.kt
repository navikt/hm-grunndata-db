package no.nav.hm.grunndata.db.hmdb

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import io.micronaut.data.exceptions.DataAccessException
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.hm.grunndata.db.rapid.EventNames
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import no.nav.hm.grunndata.db.supplier.toDTO
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Context
@Requires(bean = KafkaRapid::class)
class SupplierSyncScheduler(private val supplierRepository: SupplierRepository,
                            private val hmdbBatchRepository: HmDbBatchRepository,
                            private val hmDbClient: HmDbClient,
                            private val rapidPushService: RapidPushService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierSyncScheduler::class.java)
    }

    init {
        runBlocking {
            hmdbBatchRepository.findByName(SYNC_SUPPLIERS) ?: syncSuppliers()
        }
    }

    @Scheduled(cron = "0 15 0 * * *")
    fun syncSuppliers() {
        runBlocking {
            val syncBatchJob = hmdbBatchRepository.findByName(SYNC_SUPPLIERS) ?: hmdbBatchRepository.save(
                HmDbBatch(
                    name = SYNC_SUPPLIERS,
                    syncfrom = LocalDateTime.now().minusYears(20).truncatedTo(ChronoUnit.SECONDS)
                )
            )
            hmDbClient.fetchSuppliers(syncBatchJob.syncfrom)?.let { suppliers ->

                LOG.info("Calling supplier sync from ${syncBatchJob.syncfrom}, Got total of ${suppliers.size} suppliers")
                val entities = suppliers.map { it.toSupplier() }.sortedBy { it.updated }
                runBlocking {
                    entities.forEach {
                        val saved = supplierRepository.findByIdentifier(it.identifier)?.let { inDb ->
                            supplierRepository.update(
                                it.copy(
                                    id = inDb.id,
                                    created = inDb.created,
                                    createdBy = inDb.createdBy
                                )
                            )
                        } ?: run {
                            try {
                                supplierRepository.save(it)
                            } catch (e: DataAccessException) {
                                LOG.error("Got exception ${e.message}")
                                supplierRepository.save(it.copy(name = it.name + " DUPLICATE"))
                            }
                        }
                        LOG.info("saved supplier ${saved.id} with identifier ${saved.identifier} and lastupdated ${saved.updated}")
                        rapidPushService.pushToRapid(
                            key = "${EventNames.hmdbsuppliersync}-${saved.id}",
                            eventName = EventNames.hmdbsuppliersync, payload = saved.toDTO()
                        )
                    }
                    hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = suppliers.last().lastupdated))
                }
            }
        }
    }
}