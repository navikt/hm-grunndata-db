package no.nav.hm.grunndata.db

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.micronaut.context.annotation.Factory
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import jakarta.inject.Singleton

@Factory
class JacksonConfig : BeanCreatedEventListener<ObjectMapper> {

    @Singleton
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().findAndRegisterModules()
    }

    override fun onCreated(event: BeanCreatedEvent<ObjectMapper>): ObjectMapper {
        LOG.debug("Initialized JacksonConfig")
        val objectMapper = event.bean
        objectMapper.registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, false)
        return objectMapper
    }

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(JacksonConfig::class.java)
    }
}
