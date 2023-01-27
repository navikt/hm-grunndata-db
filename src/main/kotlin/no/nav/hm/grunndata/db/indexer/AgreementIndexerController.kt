package no.nav.hm.grunndata.db.indexer

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import kotlinx.coroutines.flow.*
import no.nav.hm.grunndata.db.agreement.AgreementRepository
import org.slf4j.LoggerFactory


@Controller("/internal/index")
class AgreementIndexerController(
    private val indexer: AgreementIndexer,
    private val agreementRepository: AgreementRepository
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexerController::class.java)
    }

    @Put("/agreements")
    suspend fun indexAgreements() {
        LOG.info("Indexing all agreements")
        agreementRepository.findAll()
            .onEach {
                indexer.index(it.toDoc())
            }
            .catch { e -> LOG.error("Got exception while indexing ${e.message}") }
            .collect()
    }

    @Post("/agreements/{indexName}")
    suspend fun indexAgreements(indexName: String) {
        if (!indexer.indexExists(indexName)) indexer.createIndex(indexName)
        LOG.info("index to $indexName")
        agreementRepository.findAll()
            .onEach {
                indexer.index(it.toDoc(), indexName)
            }
            .catch { e -> LOG.error("Got exception while indexing ${e.message}") }
            .collect()
    }

    @Put("/agreements/alias/{indexName}")
    fun indexAliasTo(indexName: String) {
        LOG.info("Changing alias to $indexName")
        indexer.updateAlias(indexName)
    }

}
