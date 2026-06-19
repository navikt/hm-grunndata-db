package no.nav.hm.grunndata.db

import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper

@Factory
class JacksonConfig {

    @Singleton
    fun objectMapper(): ObjectMapper {
        return JsonMapper.builderWithJackson2Defaults()
            .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .changeDefaultPropertyInclusion() { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, false)
            .build()
    }

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(JacksonConfig::class.java)
    }
}
