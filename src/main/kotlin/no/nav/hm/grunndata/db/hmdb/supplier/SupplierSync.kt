package no.nav.hm.grunndata.db.hmdb.supplier

import io.micronaut.context.annotation.Requires
import io.micronaut.data.exceptions.DataAccessException
import jakarta.inject.Singleton
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.hm.grunndata.db.GdbRapidPushService
import no.nav.hm.grunndata.db.hmdb.*
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.db.supplier.toDTO
import no.nav.hm.grunndata.rapid.event.EventName
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Singleton
@Requires(bean = KafkaRapid::class)
class SupplierSync(
    private val supplierService: SupplierService,
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
            persistAndPushToRapid(suppliers)
            hmdbBatchRepository.update(syncBatchJob.copy(syncfrom = suppliers.last().lastupdated))
        }
    }

    private fun persistAndPushToRapid(suppliers: List<HmdbSupplierDTO>) {
        val entities = suppliers.map { it.toSupplier() }.sortedBy { it.updated }
        entities.forEach {
            val saved = supplierService.findByIdentifier(it.identifier)?.let { inDb ->
                supplierService.update(
                        it.copy(
                            id = inDb.id,
                            identifier = inDb.identifier,
                            created = inDb.created,
                            createdBy = inDb.createdBy
                        )
                    )

            } ?: run {
                try {
                    supplierService.save(it)
                } catch (e: DataAccessException) {
                    LOG.error("Got exception ${e.message}")
                    supplierService.save(it.copy(name = it.name + " DUPLICATE"))
                }
            }
            LOG.info("saved supplier ${saved.id} with identifier ${saved.identifier} and lastupdated ${saved.updated}")
            gdbRapidPushService.pushDTOToKafka(saved.toDTO(), EventName.hmdbsuppliersyncV1)
        }
    }

    suspend fun syncAllSuppliers() {
        LOG.info("Sync all suppliers")
        hmDbClient.fetchAllSuppliers()?.let { persistAndPushToRapid(it) }
    }

}
