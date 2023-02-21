package no.nav.hm.grunndata.db.hmdb

import io.micronaut.context.annotation.Requires
import io.micronaut.data.exceptions.DataAccessException
import jakarta.inject.Singleton
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import no.nav.hm.grunndata.db.supplier.toDTO
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Singleton
@Requires(bean = KafkaRapid::class)
class SupplierSync(
    private val supplierRepository: SupplierRepository,
    private val hmdbBatchRepository: HmDbBatchRepository,
    private val hmDbClient: HmDbClient,
    private val gdbRapidPushService: GdbRapidPushService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierSync::class.java)
    }

    suspend fun syncSuppliers() {

        val syncBatchJob = hmdbBatchRepository.findByName(SYNC_SUPPLIERS) ?: hmdbBatchRepository.save(
            HmDbBatch(
                name = SYNC_SUPPLIERS,
                syncfrom = LocalDateTime.now().minusYears(20).truncatedTo(ChronoUnit.SECONDS)
            )
        )
        hmDbClient.fetchSuppliers(syncBatchJob.syncfrom)?.let { suppliers ->
            LOG.info("Calling supplier sync from ${syncBatchJob.syncfrom}, Got total of ${suppliers.size} suppliers")
            val entities = suppliers.map { it.toSupplier() }.sortedBy { it.updated }

            entities.forEach {
                val saved = supplierRepository.findByIdentifier(it.identifier)?.let { inDb ->
                    if (it.updated.isAfter(inDb.updated)) { // hack to fix duplicate errors
                            supplierRepository.update(
                                it.copy(
                                    id = inDb.id,
                                    created = inDb.created,
                                    createdBy = inDb.createdBy
                                )
                            )
                        }
                    else inDb

                } ?: run {
                    try {
                        supplierRepository.save(it)
                    } catch (e: DataAccessException) {
                        LOG.error("Got exception ${e.message}")
                        supplierRepository.save(it.copy(name = it.name + " DUPLICATE"))
                    }
                }
                LOG.info("saved supplier ${saved.id} with identifier ${saved.identifier} and lastupdated ${saved.updated}")
                gdbRapidPushService.pushDTOToKafka(saved.toDTO(), EventName.hmdbsuppliersync)
            }
            hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = suppliers.last().lastupdated))
        }
    }

}