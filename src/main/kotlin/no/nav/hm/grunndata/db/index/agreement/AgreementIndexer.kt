package no.nav.hm.grunndata.db.index.agreement

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.agreement.AgreementCriteria
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.agreement.toDTO
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.Indexer
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.index.item.IndexType
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Singleton
class AgreementIndexer(private val agreementService: AgreementService,
                       private val indexableItem: AgreementIndexItem,
                       private val indexer: Indexer) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexer::class.java)

    }

    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(indexableItem.getAliasIndexName())
        if (!indexer.indexExists(indexName)) {
            LOG.info("creating index $indexName")
            indexer.createIndex(indexName, indexableItem.getSettings(), indexableItem.getMappings())
        }
        val updated =  LocalDateTime.now().minusYears(30)
        val criteria = AgreementCriteria(updatedAfter = updated)
        val page = agreementService.findAll(criteria, Pageable.from(0, 5000, Sort.of(Sort.Order.asc("updated"))))
        val agreements = page.content.map { IndexDoc(id = it.id, indexType = IndexType.AGREEMENT, doc = it.toDTO().toDoc(), indexName = indexName)}
        LOG.info("indexing ${agreements.size} agreements to $indexName")
        if (agreements.isNotEmpty()) indexer.indexDoc(agreements)
        if (alias) {
           indexer.updateAlias(indexableItem.getAliasIndexName(), indexName)
        }
    }

    suspend fun updateAlias(indexName: String) {
        indexer.updateAlias(indexableItem.getAliasIndexName(), indexName)
    }

    fun getAlias() = indexer.getAlias(indexableItem.getAliasIndexName())

}
