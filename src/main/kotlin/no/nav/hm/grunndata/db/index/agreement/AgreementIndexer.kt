package no.nav.hm.grunndata.db.index.agreement

import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.agreement.AgreementCriteria
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.agreement.toDTO
import no.nav.hm.grunndata.db.index.IndexName
import no.nav.hm.grunndata.db.index.Indexer
import no.nav.hm.grunndata.db.index.createIndexName
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import no.nav.hm.grunndata.rapid.dto.AgreementStatus
import org.opensearch.client.opensearch.OpenSearchClient

@Singleton
class AgreementIndexer(private val agreementService: AgreementService,
                       @Value("\${agreements.aliasName}") private val aliasName: String,
                       private val client: OpenSearchClient): Indexer(client, settings, mapping, aliasName,) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexer::class.java)
        val settings = AgreementIndexer::class.java
        .getResource("/opensearch/agreements_settings.json")!!.readText()
        val mapping = AgreementIndexer::class.java
        .getResource("/opensearch/agreements_mapping.json")!!.readText()
    }

    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(IndexName.agreements)
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName, settings, mapping)
        }
        val updated =  LocalDateTime.now().minusYears(30)
        val criteria = AgreementCriteria(updatedAfter = updated)
        val page = agreementService.findAll(criteria, Pageable.from(0, 5000, Sort.of(Sort.Order.asc("updated"))))
        val agreements = page.content.map { it.toDTO().toDoc() }.filter {  it.status != AgreementStatus.DELETED }
        LOG.info("indexing ${agreements.size} agreements to $indexName")
        if (agreements.isNotEmpty()) index(agreements, indexName)
        if (alias) {
           updateAlias(indexName)
        }
    }
}
