package no.nav.hm.grunndata.db.hmdb.product

class HmDbProductBatchDTO(
    val products: List<HmDbProductDTO>,
    val blobs: Map<Long, List<BlobDTO>>,
    val techdata: Map<Long, List<TechDataDTO>>
)
