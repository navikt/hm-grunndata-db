package no.nav.hm.grunndata.db.hmdb.agreement

import java.time.LocalDateTime

data class NewsDocDTO(
    val hmidocid: Long,
    val newsId: Long,
    val hmidoctitle: String,
    val hmidocdesc: String?,
    val hmidocfile: List<String> = emptyList(),
    val hmidocindate: LocalDateTime
)