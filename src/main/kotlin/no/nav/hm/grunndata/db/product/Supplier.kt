package no.nav.hm.grunndata.db.product

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Table

@MappedEntity
@Table(name="supplier_v1")
data class Supplier(
    @field:GeneratedValue
    @field:Id
    var id:         Long=-1L,
    val uuid:       String = UUID.randomUUID().toString(),
    @field:TypeDef(type=DataType.JSON)
    val info: SupplierInfo,
    val created:    LocalDateTime = LocalDateTime.now(),
    val updated:    LocalDateTime = LocalDateTime.now()
)

data class SupplierInfo (
    val name: String,
    val address: String?=null,
    val email: String?=null,
    val phone: String?=null,
    val homepage: String?=null
)