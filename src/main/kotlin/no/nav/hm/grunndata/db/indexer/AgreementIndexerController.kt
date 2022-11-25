package no.nav.hm.grunndata.db.indexer

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import kotlinx.coroutines.flow.*
import no.nav.hm.grunndata.db.agreement.AgreementDocument
import no.nav.hm.grunndata.db.agreement.AgreementPostRepository
import no.nav.hm.grunndata.db.agreement.AgreementRepository
import no.nav.hm.grunndata.db.product.ProductRepository
import no.nav.hm.grunndata.db.supplier.SupplierRepository
import org.slf4j.LoggerFactory


@Controller("/internal/index")
class AgreementIndexerController(private val indexer: AgreementIndexer,
                                 private val agreementRepository: AgreementRepository,
                                 private val agreementPostRepository: AgreementPostRepository
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexerController::class.java)
    }

    @Put("/agreements")
    suspend fun indexAgreements() {
        LOG.info("Indexing all agreements")
        agreementRepository.findAll()
            .onEach {
                val document = AgreementDocument (it, agreementPostRepository.findByAgreementId(it.id))
                indexer.index(document.toDoc())
            }
            .catch { e -> LOG.error("Got exception while indexint ${e.message}") }
            .collect()
    }

    @Post("/agreements/{indexName}")
    suspend fun indexAgreements(indexName: String) {
        LOG.info("creating index $indexName")
        val success = indexer.createIndex(indexName)
        if (success) {
            LOG.info("index to $indexName")
            agreementRepository.findAll()
                .onEach {
                    val document = AgreementDocument (it, agreementPostRepository.findByAgreementId(it.id))
                    indexer.index(document.toDoc())
                }
                .catch { e -> LOG.error("Got exception while indexint ${e.message}") }
                .collect()
        }
    }

    @Put("/agreements/alias/{indexName}")
    fun indexAliasTo(indexName:String) {
        LOG.info("Changing alias to $indexName")
        indexer.updateAlias(indexName)
    }

}
