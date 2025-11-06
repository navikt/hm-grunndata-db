package no.nav.hm.grunndata.db.product

import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.register.leaderelection.LeaderOnly
import java.time.LocalDateTime

@Singleton
open class ProductDeleteScheduler(private val productService: ProductService) {

    @Scheduled(cron = "0 */5 * * * *")
    @LeaderOnly
    open fun deleteProducts() {
        runBlocking {
            val toBeDeleted = productService.findDeletedStatusUpdatedBefore(LocalDateTime.now().minusMinutes(30))
            if (toBeDeleted.isNotEmpty()) {
                productService.deleteProducts(toBeDeleted)
            }
        }
    }

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(ProductDeleteScheduler::class.java)
    }
}