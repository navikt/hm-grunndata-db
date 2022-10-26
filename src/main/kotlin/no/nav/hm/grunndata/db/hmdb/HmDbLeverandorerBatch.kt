package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierInfo
import java.time.LocalDateTime
import javax.persistence.Table

@MappedEntity
@Table(name="hmdbleverandorbatch_v1")
data class HmDbLeverandorerBatch(
    @field:GeneratedValue
    @field:Id
    var id: Long = -1L,
    @field:TypeDef(type = DataType.JSON)
    val leverandorer: List<LeverandorDTO>,
    val created: LocalDateTime = LocalDateTime.now()
)

data class HmDbLeverandorerBatchDTO(
    val leverandorer: List<LeverandorDTO>
)

fun HmDbLeverandorerBatchDTO.toEntity() : HmDbLeverandorerBatch = HmDbLeverandorerBatch(
    leverandorer = leverandorer
)

fun HmDbLeverandorerBatch.toSupplierList() : List<Supplier> = leverandorer.map {
        Supplier(id=it.leverandorid.toLong(), hmdbId = it.leverandorid.toLong(), name = it.leverandornavn!!, info = SupplierInfo(
            address = it.adresse, email = it.epost, phone = it.telefon, homepage = it.www
        ))
    }

