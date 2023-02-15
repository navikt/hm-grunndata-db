package no.nav.hm.grunndata.db.hmdb

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.hm.grunndata.db.LeaderElection
import no.nav.hm.grunndata.db.agreement.*
import no.nav.hm.grunndata.db.hmdb.agreement.*
import no.nav.hm.grunndata.db.rapid.EventNames
import no.nav.hm.grunndata.dto.AgreementAttachment
import no.nav.hm.grunndata.dto.AgreementPost
import no.nav.hm.grunndata.dto.Media
import no.nav.hm.grunndata.dto.MediaType
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Singleton
@Requires(bean = KafkaRapid::class)
@Requires(property = "schedulers.enabled", value = "true")
class AgreementSyncScheduler(private val agreementSync: AgreementSync) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementSyncScheduler::class.java)
    }

    @Scheduled(cron="0 30 0 * * *")
    fun syncAgreements() {
        agreementSync.syncAgreements()
    }

}