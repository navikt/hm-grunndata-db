package no.nav.hm.grunndata.db.hmdb

import java.time.LocalDate
import java.time.LocalDateTime

data class HmDbProductDTO (
    val artid: Long,
    val adescshort: String?,
    val adraft: Boolean?,
    val aindate: LocalDateTime,
    val achange: LocalDateTime,
    val aisapproved: Boolean?,
    val anbudid: String?,
    val aout: Boolean?,
    val aoutdate: LocalDateTime?,
    val apostid: Long?,
    val postrank: Int?,
    val artname: String,
    val artno: String?,
    val artpostid: String?,
    val hasanbud: Boolean?,
    val isactive: Boolean?,
    val isocode: String,
    val newsid: String?,
    val newspublish: LocalDate?,
    val newsexpire: LocalDate?,
    val prodid: Long,
    val pchange: LocalDateTime,
    val prodname: String,
    val pshortdesc: String,
    val stockid: String?,
    val supplier: String?
)
