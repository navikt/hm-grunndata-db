package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.annotation.MappedEntity
import java.time.LocalDateTime

data class NewsDTO (
    val newsid: Long,
    val newstitle: String,
    val newsresume: String?,
    val newstext: String?,
    val newslink: String?,
    val newstype: Int,
    val newspublisher: Long,
    val newspublish: LocalDateTime,
    val newsexpire: LocalDateTime?,
    val externid: String?
)
