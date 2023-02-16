package no.nav.hm.grunndata.db.product

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import jakarta.persistence.Column
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.rapid.dto.*
import java.time.LocalDateTime
import java.util.UUID

@MappedEntity("product_v1")
data class Product (
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val supplierId: UUID,
    val title: String,
    @field:TypeDef(type = DataType.JSON)
    val attributes: Map<AttributeNames, Any>,
    val status: ProductStatus = ProductStatus.ACTIVE,
    @field:Column(name="hms_artnr")
    val hmsArtNr: String?=null,
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

fun ProductDTO.toEntity(): Product = Product (
    id = id, supplierId = supplier.id, title = title, attributes=attributes, status = status, hmsArtNr = hmsArtNr,
    identifier = identifier, supplierRef=supplierRef, isoCategory=isoCategory, accessory=accessory, sparePart=sparePart,
    seriesId=seriesId, techData=techData, media= media, created=created, updated=updated, published=published, expired=expired,
    agreementInfo = agreementInfo, createdBy=createdBy, updatedBy=updatedBy
)

