package no.nav.hm.grunndata.db.hmdb

import java.time.LocalDateTime

class ProductBatchDTO(
    val changedAfter: LocalDateTime,
    val products: List<ProductDTO>,
    val blobs: Map<Long, List<BlobDTO>>,
    val techdata: Map<Long, List<TechDataDTO>>
)
