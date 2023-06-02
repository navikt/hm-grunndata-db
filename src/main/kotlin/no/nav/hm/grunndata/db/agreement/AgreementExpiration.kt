package no.nav.hm.grunndata.db.agreement

import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.product.ProductService
import no.nav.hm.grunndata.rapid.dto.AgreementStatus
import no.nav.hm.grunndata.rapid.event.EventName
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import javax.transaction.Transactional

@Singleton
open class AgreementExpiration(private val agreementService: AgreementService,
                               private val productService: ProductService) {

    companion object {
        private const val updatedBy = "AGREEMENTEXPIRATION"
        private val LOG = LoggerFactory.getLogger(AgreementExpiration::class.java)

    }
    suspend fun expiredAgreements() {
        val expiredList = agreementService.findByStatusAndExpiredBefore(AgreementStatus.ACTIVE)
        expiredList.forEach {
            deactiveProductsInAgreement(it)
        }
    }

    @Transactional
    open suspend fun deactiveProductsInAgreement(agreement: Agreement) {
        LOG.info("Agreement ${agreement.id} ${agreement.reference} has expired")
        agreementService.update(agreement.copy(status = AgreementStatus.INACTIVE,
            updated = LocalDateTime.now(), updatedBy = updatedBy))
        val productsInAgreement = productService.findByAgreementId(agreement.id)
        productsInAgreement.forEach { product ->
            LOG.info("Found ${product.id} in expired agreement")
            val notExpired = product.agreements?.filterNot {
                it.id == agreement.id
            }
            productService.saveAndPushTokafka(product.copy(agreements = notExpired,
                updated = LocalDateTime.now(), updatedBy = updatedBy), eventName = EventName.hmdbproductsyncV1)
        }
    }
}
