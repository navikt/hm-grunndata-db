package no.nav.hm.grunndata.db.hmdb.product

import java.time.LocalDateTime

class HmDbProductBatchDTO(
    val changeTo: LocalDateTime,
    val products: List<HmDbProductDTO>,
    val blobs: Map<Long, List<BlobDTO>>,
    val techdata: Map<Long, List<TechDataDTO>>
)
