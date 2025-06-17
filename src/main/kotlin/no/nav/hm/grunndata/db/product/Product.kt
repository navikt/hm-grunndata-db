package no.nav.hm.grunndata.db.product

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import jakarta.persistence.Column
import no.nav.hm.grunndata.rapid.dto.*
import java.time.LocalDateTime
import java.util.UUID
import no.nav.hm.grunndata.db.REGISTER

@MappedEntity("product_v1")
data class Product(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val supplierId: UUID,
    val title: String,
    val articleName: String,
    @field:TypeDef(type = DataType.JSON)
    val attributes: Attributes,
    val status: ProductStatus = ProductStatus.ACTIVE,
    @field:Column(name = "hms_artnr")
    val hmsArtNr: String? = null,
    val identifier: String,
    val supplierRef: String,
    val isoCategory: String,
    val accessory: Boolean = false,
    val sparePart: Boolean = false,
    val mainProduct: Boolean = true,
    val seriesUUID: UUID?,
    @Deprecated("Use seriesUUID instead")
    val seriesId: String? = null,
    val seriesIdentifier: String? = null,
    @field:TypeDef(type = DataType.JSON)
    val techData: List<TechData> = emptyList(),
    @field:TypeDef(type = DataType.JSON)
    val media: Set<MediaInfo> = emptySet(),
    @field:TypeDef(type = DataType.JSON)
    @field:Column(name = "agreement")
    @Deprecated("Use agreements instead")
    val agreementInfo: AgreementInfo? = null,
    @field:TypeDef(type = DataType.JSON)
    val agreements: Set<ProductAgreement>? = emptySet(),
    @field:TypeDef(type = DataType.JSON)
    val pastAgreements: Set<ProductAgreement> = emptySet(),
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val published: LocalDateTime = LocalDateTime.now(),
    val expired: LocalDateTime = updated.plusYears(20),
    val createdBy: String = REGISTER,
    val updatedBy: String = REGISTER
)

data class ProductAgreement(
    val id: UUID,
    val title: String? = null,
    val articleName: String? = "",
    val identifier: String? = null,
    val reference: String,
    val rank: Int,
    val postNr: Int,
    val postIdentifier: String? = null,
    val postId: UUID? = null,
    val published: LocalDateTime? = null,
    val expired: LocalDateTime? = null,
    val status: ProductAgreementStatus? = null,
    val mainProduct: Boolean = true,
    val accessory: Boolean = false,
    val sparePart: Boolean = false
)

fun ProductRapidDTO.toEntity(): Product = Product(
    id = id,
    supplierId = supplier.id,
    title = title,
    articleName = articleName,
    attributes = attributes,
    status = status,
    hmsArtNr = hmsArtNr,
    identifier = identifier,
    supplierRef = supplierRef,
    isoCategory = isoCategory,
    accessory = accessory,
    sparePart = sparePart,
    mainProduct = mainProduct,
    seriesUUID = seriesUUID,
    seriesId = seriesId,
    techData = techData,
    media = media,
    created = created,
    updated = updated,
    published = published,
    expired = expired,
    agreements = agreements.map { it.toProductAgreement() }.toSet(),
    agreementInfo = agreementInfo,
    createdBy = createdBy,
    updatedBy = updatedBy
)

private fun AgreementInfo.toProductAgreement(): ProductAgreement = ProductAgreement(
    id = id, title = title, identifier = identifier, reference = reference, rank = rank, postNr = postNr,
    postIdentifier = postIdentifier, postId = postId, published = published, expired = expired, status = status,
    articleName = articleName, mainProduct = mainProduct, accessory = accessory, sparePart = sparePart
)