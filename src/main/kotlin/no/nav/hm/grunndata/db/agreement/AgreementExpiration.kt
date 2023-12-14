package no.nav.hm.grunndata.db.agreement

import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import no.nav.hm.grunndata.db.product.ProductAgreement
import no.nav.hm.grunndata.db.product.ProductService
import no.nav.hm.grunndata.rapid.dto.AgreementStatus
import no.nav.hm.grunndata.rapid.event.EventName
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Singleton
open class AgreementExpiration(private val agreementService: AgreementService,
                               private val productService: ProductService) {

//    companion object {
//        private const val expiration = "AGREEMENTEXPIRATION"
//        private val LOG = LoggerFactory.getLogger(AgreementExpiration::class.java)
//
//    }
//    suspend fun expiredAgreements() {
//        val expiredList = agreementService.findByStatusAndExpiredBefore(AgreementStatus.ACTIVE)
//        expiredList.forEach {
//            deactiveProductsInExpiredAgreement(it)
//        }
//    }
//
//    @Transactional
//    open suspend fun deactiveProductsInExpiredAgreement(expiredAgreement: Agreement) {
//        LOG.info("Agreement ${expiredAgreement.id} ${expiredAgreement.reference} has expired")
//        agreementService.saveAndPushTokafka(agreement = expiredAgreement.copy(status = AgreementStatus.INACTIVE,
//            updated = LocalDateTime.now(), updatedBy = expiration), eventName = EventName.expiredAgreementV1)
//        val productsInAgreement = productService.findByAgreementId(expiredAgreement.id)
//        productsInAgreement.forEach { product ->
//            LOG.info("Found product: ${product.id} in expired agreement")
//            val expiredProductAgreements = product.agreements?.filter {
//                it.id == expiredAgreement.id
//            }?.toSet()
//            val notExpired = product.agreements?.filterNot {
//                it.id == expiredAgreement.id
//            }?.toSet()
//            productService.saveAndPushTokafka(product.copy(agreements = notExpired,
//                pastAgreements = product.pastAgreements.plus(expiredProductAgreements) as Set<ProductAgreement>,
//                updated = LocalDateTime.now(), updatedBy = expiration), eventName = EventName.expiredProductAgreementV1)
//        }
//    }
}
