package no.nav.hm.grunndata.db.hmdb.news

import java.time.LocalDateTime

data class HMDNewsDTO (
    val newsid: Long,
    val newstitle: String,
    val newsresume: String?,
    val newstext: String?,
    val newslink: String?,
    val newstype: Int,
    val newspublisher: Long,
    val newspublish: LocalDateTime,
    val newsexpire: LocalDateTime?,
    val ldbid: Int,
    val externid: String?,
)