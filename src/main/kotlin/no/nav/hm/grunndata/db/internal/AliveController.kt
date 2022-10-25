package no.nav.hm.grunndata.db.internal

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/internal")
class AliveController {

    @Get("/alive")
    fun alive(): HttpResponse<String> {
        return HttpResponse.ok("OK")
    }

    @Get("/ready")
    fun ready(): HttpResponse<String> {
        return HttpResponse.ok("OK")
    }

}