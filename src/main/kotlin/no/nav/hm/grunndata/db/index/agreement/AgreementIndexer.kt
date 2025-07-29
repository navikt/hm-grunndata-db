package no.nav.hm.grunndata.db.index.agreement

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.agreement.AgreementCriteria
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.agreement.toDTO
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.indexSettingsMap
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Singleton
class AgreementIndexer(private val agreementService: AgreementService,
                       private val indexer: OpensearchIndexer) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexer::class.java)
        val settings = AgreementIndexer::class.java
            .getResource("/opensearch/agreements_settings.json")!!.readText()
        val mapping = AgreementIndexer::class.java
            .getResource("/opensearch/agreements_mapping.json")!!.readText()
    }

    val aliasIndexName: String = indexSettingsMap[IndexType.AGREEMENT]!!.aliasIndexName

    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(aliasIndexName)
        if (!indexer.indexExists(indexName)) {
            LOG.info("creating index $indexName")
            indexer.createIndex(indexName, settings, mapping)
        }
        val updated =  LocalDateTime.now().minusYears(30)
        val criteria = AgreementCriteria(updatedAfter = updated)
        val page = agreementService.findAll(criteria, Pageable.from(0, 5000, Sort.of(Sort.Order.asc("updated"))))
        val agreements = page.content.map { IndexDoc(id = it.id.toString(), indexType = IndexType.AGREEMENT, doc = it.toDTO().toDoc(), indexName = indexName)}
        LOG.info("indexing ${agreements.size} agreements to $indexName")
        if (agreements.isNotEmpty()) indexer.indexDoc(agreements)
        if (alias) {
           indexer.updateAlias(aliasIndexName, indexName)
        }
    }

    fun updateAlias(indexName: String) {
        indexer.updateAlias(aliasIndexName, indexName)
    }

    fun getAlias() = indexer.getAlias(aliasIndexName)

}
