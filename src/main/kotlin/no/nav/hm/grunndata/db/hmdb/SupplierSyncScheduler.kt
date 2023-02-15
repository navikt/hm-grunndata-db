package no.nav.hm.grunndata.db.hmdb

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import io.micronaut.data.exceptions.DataAccessException
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.hm.grunndata.db.LeaderElection
import no.nav.hm.grunndata.db.rapid.EventNames
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import no.nav.hm.grunndata.db.supplier.toDTO
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Singleton
@Requires(property = "schedulers.enabled", value = "true")
class SupplierSyncScheduler(private val supplierSync: SupplierSync) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierSyncScheduler::class.java)
    }

    @Scheduled(cron = "0 15 0 * * *")
    fun syncSuppliers() {
        supplierSync.syncSuppliers()
    }
}