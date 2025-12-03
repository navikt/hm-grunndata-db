package no.nav.hm.grunndata.db.index.servicejob

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.IndexDoc
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import no.nav.hm.grunndata.db.index.item.IndexSettings
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.servicejob.ServiceJobService
import org.slf4j.LoggerFactory

@Singleton
class ServiceJobIndexer(
    private val serviceJobService: ServiceJobService,
    private val indexSettings: IndexSettings,
    private val indexer: OpensearchIndexer
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ServiceJobIndexer::class.java)
    }

    val aliasIndexName = indexSettings.indexConfigMap[IndexType.SERVICEJOB]!!.aliasIndexName

    suspend fun reIndex(alias: Boolean) {
        val indexName = indexSettings.createIndexForReindex(IndexType.SERVICEJOB)
        val page = serviceJobService.findServiceJobs(
            buildCriteriaSpec = null,
            Pageable.from(0, 5000, Sort.of(Sort.Order.asc("updated")))
        )
        val jobs = page.content.map {
            IndexDoc(
                id = it.id.toString(),
                indexType = IndexType.SERVICEJOB,
                doc = it.toDoc(),
                indexName = indexName
            )
        }
        LOG.info("indexing ${jobs.size} service jobs to $indexName")
        indexer.indexDoc(jobs)
        if (alias) {
            indexer.updateAlias(aliasIndexName, indexName)
        }
    }

    fun updateAlias(indexName: String) = indexer.updateAlias(indexName, indexName)
    fun getAlias() = indexer.getAlias(aliasIndexName)
    fun docCount() = indexer.docCount(aliasIndexName)
}
