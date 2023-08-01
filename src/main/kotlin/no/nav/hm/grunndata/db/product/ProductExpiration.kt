package no.nav.hm.grunndata.db.product

import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.event.EventName
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Singleton
open class ProductExpiration(private val productService: ProductService) {

    companion object {
        private const val expiration = "PRODUCTEXPIRATION"
        private val LOG = LoggerFactory.getLogger(ProductExpiration::class.java)

    }
    suspend fun expiredProducts() {
        val expiredList = productService.findByStatusAndExpiredBefore(ProductStatus.ACTIVE)
        expiredList.forEach {
            deactiveExpiredProducts(it)
        }
    }

    @Transactional
    open suspend fun deactiveExpiredProducts(expiredProduct: Product) {
        LOG.info("Product ${expiredProduct.id} ${expiredProduct.supplierRef} has expired")
        productService.saveAndPushTokafka(expiredProduct.copy(status = ProductStatus.INACTIVE,
            updated = LocalDateTime.now(), updatedBy = expiration), eventName = EventName.expiredProductV1)
    }
}
