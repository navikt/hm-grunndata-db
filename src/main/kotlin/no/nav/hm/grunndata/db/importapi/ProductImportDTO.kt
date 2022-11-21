package no.nav.hm.grunndata.db.importapi

import no.nav.hm.grunndata.db.product.*
import java.time.LocalDateTime

data class ProductImportDTO(
    val supplierId: Long,
    val title: String,
    val description: Description,
    val status: ProductStatus = ProductStatus.ACTIVE,
    val HMSArtNr: String?=null,
    val supplierRef: String,
    val isoCategory: String,
    val accessory: Boolean = false,
    val part: Boolean = false,
    val seriesId: String?=null,
    val techData: List<TechData> = emptyList(),
    val media: List<Media> = emptyList(),
    val created: LocalDateTime?=null,
    val expired: LocalDateTime?=null,
    val createdBy: String = "import-api",
    val updatedBy: String = "import-api"
)

fun ProductImportDTO.toEntity(): Product = Product(
    title = title,
    seriesId = seriesId,
    HMSArtNr = HMSArtNr,
    isoCategory = isoCategory,
    techData = techData,
    supplierId = supplierId,
    supplierRef = supplierRef,
    identifier = supplierRef,
    description = description,
    accessory = accessory,
    sparepart = part,
    media = media,
    created = created?: LocalDateTime.now(),
    expired = expired?: LocalDateTime.now().plusYears(10)
)
