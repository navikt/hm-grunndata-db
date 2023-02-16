package no.nav.hm.grunndata.db.supplier

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.rapid.dto.SupplierDTO
import no.nav.hm.grunndata.rapid.dto.SupplierInfo
import no.nav.hm.grunndata.rapid.dto.SupplierStatus
import java.time.LocalDateTime
import java.util.*

@MappedEntity(SupplierTableName)
data class Supplier(
    @field:Id
    val id:       UUID = UUID.randomUUID(),
    val identifier: String,
    val name:       String,
    val status: SupplierStatus,
    @field:TypeDef(type=DataType.JSON)
    val info: SupplierInfo,
    val createdBy: String = HMDB,
    val updatedBy: String = HMDB,
    val created:    LocalDateTime = LocalDateTime.now(),
    val updated:    LocalDateTime = LocalDateTime.now()
)

const val SupplierTableName="supplier_v1"


fun Supplier.toDTO(): SupplierDTO = SupplierDTO(id, identifier, status,  name, info, createdBy, updatedBy, created, updated)
