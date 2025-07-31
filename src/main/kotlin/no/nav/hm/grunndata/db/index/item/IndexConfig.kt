package no.nav.hm.grunndata.db.index.item

data class IndexConfig(
    val aliasIndexName: String,
    val mappings: String,
    val settings: String,
    val indexType: IndexType,
    val enabled: Boolean = true,
)


