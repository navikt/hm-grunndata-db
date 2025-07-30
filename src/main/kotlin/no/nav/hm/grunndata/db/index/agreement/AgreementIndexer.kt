package no.nav.hm.grunndata.db.index.agreement

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.agreement.AgreementCriteria
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.agreement.toDTO
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.item.IndexSettings
import no.nav.hm.grunndata.db.index.item.IndexType
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Singleton
class AgreementIndexer(private val agreementService: AgreementService,
                       private val indexSettings: IndexSettings,
                       private val indexer: OpensearchIndexer) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexer::class.java)
    }

    val aliasIndexName: String = indexSettings.indexConfigMap[IndexType.AGREEMENT]!!.aliasIndexName

    suspend fun reIndex(alias: Boolean) {
        val indexName = indexSettings.createIndexForReindex(IndexType.AGREEMENT)
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
