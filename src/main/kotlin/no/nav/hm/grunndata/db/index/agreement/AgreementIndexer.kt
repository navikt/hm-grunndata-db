package no.nav.hm.grunndata.db.index.agreement

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.agreement.AgreementCriteria
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.agreement.toDTO
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.db.index.createIndexName
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.IndexItemSupport
import org.opensearch.client.opensearch.OpenSearchClient
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Singleton
class AgreementIndexer(private val agreementService: AgreementService,
                       private val client: OpenSearchClient) : OpensearchIndexer(client), IndexItemSupport {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexer::class.java)
        val settings = AgreementIndexer::class.java
            .getResource("/opensearch/agreements_settings.json")!!.readText()
        val mapping = AgreementIndexer::class.java
            .getResource("/opensearch/agreements_mapping.json")!!.readText()
    }

    suspend fun reIndex(alias: Boolean) {
        val indexName = createIndexName(getAliasIndexName())
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName, getSettings(), getMappings())
        }
        val updated =  LocalDateTime.now().minusYears(30)
        val criteria = AgreementCriteria(updatedAfter = updated)
        val page = agreementService.findAll(criteria, Pageable.from(0, 5000, Sort.of(Sort.Order.asc("updated"))))
        val agreements = page.content.map { IndexDoc(id = it.id, indexType = IndexType.AGREEMENT, doc = it.toDTO().toDoc(), indexName = indexName)}
        LOG.info("indexing ${agreements.size} agreements to $indexName")
        if (agreements.isNotEmpty()) indexDoc(agreements)
        if (alias) {
           updateAlias(getAliasIndexName(), indexName)
        }
    }

    suspend fun updateAlias(indexName: String) {
        updateAlias(getAliasIndexName(), indexName)
    }

    fun getAlias() = getAlias(getAliasIndexName())

    override fun getAliasIndexName(): String = "agreements"

    override fun getMappings(): String = mapping

    override fun getSettings(): String  = settings

    override fun getIndexType(): IndexType = IndexType.AGREEMENT

    override fun getSearchDocClassType(): Class<out SearchDoc> = AgreementDoc::class.java

}
