package no.nav.hm.grunndata.db

import jakarta.inject.Singleton
import no.nav.hm.grunndata.rapid.dto.RapidDTO
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.grunndata.rapid.event.RapidApp
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import java.time.LocalDateTime

@Singleton
class GdbRapidPushService(private val kafkaRapidService: RapidPushService) {

    fun pushDTOToKafka(dto: RapidDTO, eventName: String) {
        kafkaRapidService.pushToRapid(
            key = "$eventName-${dto.id}",
            eventName = eventName, payload = dto, keyValues = mapOf("createdBy" to RapidApp.grunndata_db,
                "dtoVersion" to rapidDTOVersion)
        )
    }
}
