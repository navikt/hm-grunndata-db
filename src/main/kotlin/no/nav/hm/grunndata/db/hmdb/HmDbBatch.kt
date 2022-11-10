package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import no.nav.hm.grunndata.db.supplier.Supplier
import no.nav.hm.grunndata.db.supplier.SupplierInfo
import java.time.LocalDateTime
import javax.persistence.Table

@MappedEntity
@Table(name="hmdbbatch_v1")
data class HmDbBatch(
    @field:GeneratedValue
    @field:Id
    var id: Long = -1L,
    val name: String,
    val created: LocalDateTime = LocalDateTime.now(),
    val lastupdated: LocalDateTime = LocalDateTime.now()
)

fun SupplierDTO.toSupplier(): Supplier =
        Supplier(id= adressid, hmdbId = adressid, name = adressnamn1!!, info = SupplierInfo(
            address = postadress1, email = epost, phone = telefon, homepage = www
        ))


