package no.nav.hm.grunndata.db.hmdb

import io.kotest.common.runBlocking
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@MicronautTest
class SyncHmDbSuppliersTest(private val hmDbClient: HmDbClient) {

    //@Test ignore, just for integration
    fun syncSupplierTest() = runBlocking {
       val suppliers = hmDbClient.fetchSuppliers(lastupdated = LocalDateTime.now().minusMonths(6).truncatedTo(ChronoUnit.SECONDS))
        println("We got ${suppliers?.size} suppliers from hmdb")
    }
}