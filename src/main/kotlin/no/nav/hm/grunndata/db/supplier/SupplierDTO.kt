package no.nav.hm.grunndata.db.supplier

import no.nav.hm.grunndata.db.GDB
import java.time.LocalDateTime
import java.util.*

data class SupplierDTO(
    val id: UUID,
    val identifier: String,
    val status : SupplierStatus = SupplierStatus.ACTIVE,
    val name: String,
    val info: SupplierInfo,
    val createdBy: String,
    val updatedBy: String,
    val created: LocalDateTime,
    val updated: LocalDateTime
)

data class SupplierInfo (
    val address: String?=null,
    val email: String?=null,
    val phone: String?=null,
    val homepage: String?=null
)

enum class SupplierStatus {
    INACTIVE, ACTIVE
}
