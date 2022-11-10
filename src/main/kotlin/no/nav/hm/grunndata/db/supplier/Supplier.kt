package no.nav.hm.grunndata.db.supplier

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Table

@MappedEntity
@Table(name=SupplierTableName)
data class Supplier(
    @field:GeneratedValue
    @field:Id
    var id:         Long=-1L,
    val identifier: String,
    val uuid:       String = UUID.randomUUID().toString(),
    val name:       String,
    @field:TypeDef(type=DataType.JSON)
    val info:       SupplierInfo,
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
