package no.nav.hm.grunndata.db.index

import jakarta.inject.Singleton
import no.nav.hm.grunndata.db.index.item.IndexItemService
import no.nav.hm.grunndata.db.index.item.IndexType
import no.nav.hm.grunndata.db.index.item.IndexableItem
import no.nav.hm.grunndata.db.index.product.ProductIndexer

@Singleton
class IndexerService(private val indexer: Indexer, private val indexItemService: IndexItemService) {

    var indexAbleItems : Map<IndexType,IndexableItem> = emptyMap()

    suspend fun processIndexItems(limit: Int = 1000) {
        val items = indexItemService.retrievePendingIndexItems(limit)
        if (items.isEmpty()) {
            return
        }
        val docs = items.map { item ->
            when (item.indexType) {
                IndexType.PRODUCT -> item.payload
                IndexType.AGREEMENT -> item.toDoc()
                IndexType.SUPPLIER -> item.toDoc()
                IndexType.NEWS -> item.toDoc()
                IndexType.EXTERNAL_PRODUCT -> item.toDoc()
            }
        }
    }
}