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
    val updated: LocalDateTime = LocalDateTime.now(),
    val syncfrom: LocalDateTime = LocalDateTime.now()
)

const val SYNC_NEWS="news"
const val SYNC_PRODUCTS="products"
const val SYNC_SUPPLIERS="suppliers"