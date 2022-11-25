package no.nav.hm.grunndata.db.hmdb.agreement

import java.time.LocalDate

data class AvtalePostDTO (
    val apostid: Long,
    val newsid: Long,
    val apostnr: Int,
    val aposttitle: String,
    val apostdesc: String,
    val apostindate: LocalDate
)