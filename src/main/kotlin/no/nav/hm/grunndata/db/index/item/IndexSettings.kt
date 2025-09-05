package no.nav.hm.grunndata.db.index.item

import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.OpensearchIndexer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Singleton
class IndexSettings(private val indexer: OpensearchIndexer) {

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(IndexSettings::class.java)
    }

    val indexConfigMap = mutableMapOf<IndexType, IndexConfig>().apply {
        put(
            IndexType.AGREEMENT,
            IndexConfig(
                aliasIndexName = "agreements",
                settings = IndexSettings::class.java.getResource("/opensearch/agreements_settings.json")!!.readText(),
                mappings = IndexSettings::class.java.getResource("/opensearch/agreements_mapping.json")!!.readText(),
                indexType = IndexType.AGREEMENT,
                enabled = true
            )
        )
        put(
            IndexType.NEWS,
            IndexConfig(
                aliasIndexName = "news",
                settings = IndexSettings::class.java.getResource("/opensearch/news_settings.json")!!.readText(),
                mappings = IndexSettings::class.java.getResource("/opensearch/news_mapping.json")!!.readText(),
                indexType = IndexType.NEWS,
                enabled = true
            )
        )
        put(
            IndexType.PRODUCT,
            IndexConfig(
                aliasIndexName = "products",
                settings = IndexSettings::class.java.getResource("/opensearch/products_settings.json")!!.readText(),
                mappings = IndexSettings::class.java.getResource("/opensearch/products_mapping.json")!!.readText(),
                indexType = IndexType.PRODUCT,
                enabled = true
            )
        )
        put(
            IndexType.EXTERNAL_PRODUCT,
            IndexConfig(
                aliasIndexName = "external_products",
                settings = IndexSettings::class.java.getResource("/opensearch/external_products_settings.json")!!.readText(),
                mappings = IndexSettings::class.java.getResource("/opensearch/external_products_mapping.json")!!.readText(),
                indexType = IndexType.EXTERNAL_PRODUCT,
                enabled = true
            )
        )
        put(
            IndexType.SUPPLIER,
            IndexConfig(
                aliasIndexName = "suppliers",
                settings = IndexSettings::class.java.getResource("/opensearch/suppliers_settings.json")!!.readText(),
                mappings = IndexSettings::class.java.getResource("/opensearch/suppliers_mapping.json")!!.readText(),
                indexType = IndexType.SUPPLIER,
                enabled = true
            )
        )
    }

    init {
        indexConfigMap.forEach { (type, config) ->
            if (!config.enabled) {
                LOG.info("Indexing for type: ${type.name} is disabled, skipping initialization.")
                return@forEach
            }
            LOG.info("Initializing index for type: ${type.name}, alias: ${config.aliasIndexName}")
            indexer.checkAliasIsPointingToIndex(config.aliasIndexName)
        }
    }

    fun createIndexForReindex(indexType: IndexType): String {
        val settings = indexConfigMap[indexType]
            ?: throw IllegalArgumentException("No index configuration found for type: $indexType")
        val indexName = createIndexName(settings.aliasIndexName)
        if (!indexer.indexExists(indexName)) {
            LOG.info("creating index $indexName")
            indexer.createIndex(indexName, settings.settings, settings.mappings)
        }
        return indexName
    }
}

fun createIndexName(aliasIndexName: String): String = "${aliasIndexName}_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))}"