package no.nav.hm.grunndata.db.supplier

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import no.nav.hm.grunndata.db.HMDB
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Table

@MappedEntity(SupplierTableName)
data class Supplier(
    @field:Id
    val id:       UUID = UUID.randomUUID(),
    val identifier: String,
    val name:       String,
    @field:TypeDef(type=DataType.JSON)
    val info:       SupplierInfo,
    val createdBy: String = HMDB,
    val created:    LocalDateTime = LocalDateTime.now(),
    val updated:    LocalDateTime = LocalDateTime.now()
)

data class SupplierInfo (
    val address: String?=null,
    val email: String?=null,
    val phone: String?=null,
    val homepage: String?=null
)

const val SupplierTableName="supplier_v1"

data class SupplierDTO(
    val id: UUID,
    val identifier: String,
    val name: String,
    val info: SupplierInfo,
    val createdBy: String,
    val created: LocalDateTime,
    val updated: LocalDateTime
)

fun Supplier.toDTO(): SupplierDTO = SupplierDTO(id, identifier, name, info, createdBy, created, updated)