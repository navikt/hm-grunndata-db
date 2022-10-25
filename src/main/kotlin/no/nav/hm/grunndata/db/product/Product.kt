package no.nav.hm.grunndata.db.product

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Table

@MappedEntity
@Table(name="product_v1")
data class Product (
    @field:GeneratedValue
    @field:Id
    var id: Long = -1L,
    val uuid: UUID = UUID.randomUUID(),
    val supplierId: Long,
    val title: String,
    @field:TypeDef(type = DataType.JSON)
    val description: Description,
    val status: ProductStatus = ProductStatus.ACTIVE,
    @field:Column(name="hms_artnr")
    val HMSArtNr: String?=null,
    @field:Column(name="hmdb_artid")
    val HMDBArtId: String?=null,
    val supplierRef: String,
    val isoCategory: String,
    val accessory: Boolean = false,
    val part: Boolean = false,
    val seriesId: String?=null,
    @field:TypeDef(type = DataType.JSON)
    val data: Set<TechData> = emptySet(),
    @field:TypeDef(type = DataType.JSON)
    val media: List<Media> = emptyList(),
    @field:TypeDef(type = DataType.JSON)
    val agreement: Agreement?=null,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val expired: LocalDateTime = updated.plusYears(20),
    val createdBy: String = "hjelpemiddeldatabasen",
    val updatedBy: String = "hjelpemiddeldatabasen"
)

data class Description(val modelName: String?=null,
                       val modelDescription: String?=null,
                       val text: String?=null) {

}

data class Agreement (
    val agreementId: String,
    val agreementStart: LocalDate?,
    val agreementEnd: LocalDate?,
    val agreementRank: Long=1,
    val agreementPostId: String,
    val agreementPostNr: String,
    val agreementPostTitle: String,
)

enum class ProductStatus {
    ACTIVE, INACTIVE
}

data class Media (
    val id:     Long = -1L,
    val uuid:   UUID = UUID.randomUUID(),
    val order:  Int=1,
    val type: MediaType = MediaType.IMAGE,
    val uri:    String,
    val text:   String?=null,
    val source: MediaSourceType = MediaSourceType.ONPREM
)

enum class MediaSourceType {
    ONPREM, GCP, EXTERNALURL
}

enum class MediaType {
    PDF,
    IMAGE,
    OTHER
}

data class TechData (
    val key:    String,
    val value:  String,
    val unit:   String
)

data class ProductDTO(
    var id: Long = -1L,
    val uuid: UUID = UUID.randomUUID(),
    val supplierId: Long,
    val title: String,
    val description: Description,
    val status: ProductStatus = ProductStatus.ACTIVE,
    val HMSArtNr: String?=null,
    val HMDBArtId: String?=null,
    val supplierRef: String,
    val isoCategory: String,
    val accessory: Boolean = false,
    val part: Boolean = false,
    val seriesId: String?=null,
    val data: Set<TechData> = emptySet(),
    val media: List<Media> = emptyList(),
    val agreement: Agreement?=null,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val expired: LocalDateTime = updated.plusYears(20),
    val createdBy: String = "hjelpemiddeldatabasen",
    val updatedBy: String = "hjelpemiddeldatabasen"
)

fun Product.toDTO():ProductDTO =  ProductDTO (
    id, uuid, supplierId, title, description, status, HMSArtNr, HMDBArtId, supplierRef, isoCategory, accessory, part,
    seriesId,data, media, agreement, created, updated, expired, createdBy, updatedBy
)
