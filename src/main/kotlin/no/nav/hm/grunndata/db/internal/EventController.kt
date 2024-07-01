package no.nav.hm.grunndata.db.internal

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import no.nav.hm.grunndata.db.GdbRapidPushService

@Controller("/internal")
class EventController(private val pushToRapid: GdbRapidPushService) {

    @Post("/event/bestillingsordning")
    fun createBestillingsordningsEvent() {
        // TODO: Implement
        // Good for testing, dont remove
    }
}