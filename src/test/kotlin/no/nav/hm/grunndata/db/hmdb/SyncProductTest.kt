package no.nav.hm.grunndata.db.hmdb

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.db.product.ProductRepository

@MicronautTest
class SyncProductTest(private val syncScheduler: ProductSyncScheduler,
                      private val objectMapper: ObjectMapper,
                      private val productRepository: ProductRepository) {

    //@Test ignore, just for integration
    fun syncProducts() {
        syncScheduler.syncProducts()
    }

}