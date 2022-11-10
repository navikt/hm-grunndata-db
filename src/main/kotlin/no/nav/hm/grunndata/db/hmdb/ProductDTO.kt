package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.LocalDate
import java.time.LocalDateTime

@MappedEntity
data class ProductDTO (
    @field:Id
    val artid: Long,
    val adescshort: String?,
    val adraft: Boolean?,
    val aindate: LocalDateTime,
    val achange: LocalDateTime,
    val aisapproved: Boolean?,
    val anbudid: String?,
    val aout: Boolean?,
    val aoutdate: LocalDateTime,
    val apostdesc: String?,
    val apostid: String?,
    val apostnr: String?,
    val aposttitle: String?,
    val artname: String,
    val artno: String?,
    val artpostid: String?,
    val hasanbud: Boolean?,
    val isactive: Boolean?,
    val isocode: String,
    val newsid: String?,
    val newspublish: LocalDate?,
    val newsexpire: LocalDate?,
    val postrank: Long?,
    val prodid: Long,
    val pchange: LocalDateTime,
    val prodname: String,
    val pshortdesc: String,
    val stockid: String?,
    val supplier: String?
)
