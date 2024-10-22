package no.nav.hm.grunndata.db.internal

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import no.nav.helse.rapids_rivers.KafkaRapid

@Controller("/internal")
class AliveController(private val kafkaRapid: KafkaRapid) {
    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(AliveController::class.java)
    }
    @Get("/isAlive")
    fun alive(): String {

        if (kafkaRapid.isConsumerClosed()) {
            LOG.error("Kafka consumer is closed")
            throw IllegalStateException("Kafka consumer is closed")
        }
        return "ALIVE"
    }

    @Get("/isReady")
    fun ready() = "OK"

}