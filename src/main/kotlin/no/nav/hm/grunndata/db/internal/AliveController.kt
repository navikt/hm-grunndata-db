package no.nav.hm.grunndata.db.internal

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/internal")
class AliveController {

    @Get("/alive")
    fun alive() = "ALIVE"

    @Get("/ready")
    fun ready() = "OK"

}