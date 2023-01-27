package no.nav.hm.grunndata.db.product

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import no.nav.hm.grunndata.db.HMDB
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column

@MappedEntity("product_v1")
data class Product (
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val supplierId: UUID,
    val title: String,
    @field:TypeDef(type = DataType.JSON)
    val attributes: Map<String, Any>,
    val status: ProductStatus = ProductStatus.ACTIVE,
    @field:Column(name="hms_artnr")
    val HMSArtNr: String?=null,
    val identifier: String,
    val supplierRef: String,
    val isoCategory: String,
    val accessory: Boolean = false,
    val sparePart: Boolean = false,
    val seriesId: String?=null,
    @field:TypeDef(type = DataType.JSON)
    val techData: List<TechData> = emptyList(),
    @field:TypeDef(type = DataType.JSON)
    val media: List<Media> = emptyList(),
    @field:TypeDef(type = DataType.JSON)
    @field:Column(name="agreement")
    val agreementInfo: AgreementInfo?=null,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val published: LocalDateTime = LocalDateTime.now(),
    val expired: LocalDateTime = updated.plusYears(20),
    val createdBy: String = HMDB,
    val updatedBy: String = HMDB
)

data class AgreementInfo (
    val id: UUID,
    val identifier: String?=null,
    val rank: Int,
    val postNr: Int,
    val postIdentifier: String?=null,
    val reference: String?=null,
    val expired: LocalDateTime,
)

enum class ProductStatus {
    ACTIVE, INACTIVE
}

data class Media (
    val id:   UUID = UUID.randomUUID(),
    val order:  Int=1,
    val type: MediaType = MediaType.IMAGE,
    val uri:    String,
    val text:   String?=null,
    val source: MediaSourceType = MediaSourceType.HMDB
)

enum class MediaSourceType {
    HMDB, GCP, EXTERNALURL
}

enum class MediaType {
    PDF,
    IMAGE,
    VIDEO,
    OTHER
}

data class TechData (
    val key:    String,
    val value:  String,
    val unit:   String
)

data class ProductDTO(
    val id: UUID,
    val supplierId: UUID,
    val title: String,
    val attributes: Map<String, Any>,
    val status: ProductStatus = ProductStatus.ACTIVE,
    val HMSArtNr: String?=null,
    val identifier: String,
    val supplierRef: String,
    val isoCategory: String,
    val accessory: Boolean = false,
    val sparePart: Boolean = false,
    val seriesId: String?=null,
    val techData: List<TechData> = emptyList(),
    val media: List<Media> = emptyList(),
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val published: LocalDateTime = LocalDateTime.now(),
    val expired: LocalDateTime = updated.plusYears(20),
    val agreementInfo: AgreementInfo?,
    val hasAgreement: Boolean = (agreementInfo!=null && agreementInfo.expired.isAfter(LocalDateTime.now())),
    val createdBy: String = HMDB,
    val updatedBy: String = HMDB
)

fun Product.toDTO():ProductDTO =  ProductDTO (
    id = id, supplierId = supplierId, title = title, attributes=attributes, status = status, HMSArtNr = HMSArtNr,
    identifier = identifier, supplierRef=supplierRef, isoCategory=isoCategory, accessory=accessory, sparePart=sparePart,
    seriesId=seriesId, techData=techData, media= media, created=created, updated=updated, published=published, expired=expired,
    agreementInfo = agreementInfo, hasAgreement = (agreementInfo!=null), createdBy=createdBy, updatedBy=updatedBy
)

