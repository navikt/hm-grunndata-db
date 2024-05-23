package no.nav.hm.grunndata.db.internal

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.hm.grunndata.db.hmdb.product.ProductSyncScheduler

@Controller("/internal")
class AliveController(private val productSyncScheduler: ProductSyncScheduler, private val kafkaRapid: KafkaRapid) {
    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(AliveController::class.java)
    }
    @Get("/isAlive")
    fun alive(): String {
        if (productSyncScheduler.isStopped()) {
            LOG.error("ProductSyncScheduler is stopped")
            throw IllegalStateException("ProductSyncScheduler is stopped")
        }
        if (kafkaRapid.isConsumerClosed()) {
            LOG.error("Kafka consumer is closed")
            throw IllegalStateException("Kafka consumer is closed")
        }
        return "ALIVE"
    }

    @Get("/isReady")
    fun ready() = "OK"

}