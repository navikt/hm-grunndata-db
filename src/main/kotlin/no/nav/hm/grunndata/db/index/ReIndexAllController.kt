package no.nav.hm.grunndata.db.index

import io.micronaut.http.annotation.Controller

import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import no.nav.hm.grunndata.db.index.agreement.AgreementIndexer
import no.nav.hm.grunndata.db.index.external_product.ExternalProductIndexer
import no.nav.hm.grunndata.db.index.product.ProductIndexer
import no.nav.hm.grunndata.db.index.supplier.SupplierIndexer

@Controller("/internal/index/all")
class ReIndexAllController(private val productIndexer: ProductIndexer,
                           private val supplierIndexer: SupplierIndexer,
                           private val agreementIndexer: AgreementIndexer,
                           private val externalProductIndexer: ExternalProductIndexer) {

    @Post("/")
    suspend fun indexProducts(@QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        supplierIndexer.reIndex(alias)
        agreementIndexer.reIndex(alias)
        productIndexer.reIndex(alias)
        externalProductIndexer.reIndex(alias)
    }

}
