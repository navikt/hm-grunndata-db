package no.nav.hm.grunndata.db.hmdb.agreement
import java.time.LocalDateTime

data class NewsDocHolder(
    val newsDoc: NewsDocDTO,
    val newsDocAdr: List<NewsDocAdr>
)

data class NewsDocDTO (
    val hmidocid: Long,
    val newsid: Long,
    val hmidoctitle: String?,
    val hmidocdesc: String?,
    val hmidocfilename: String?,
    val hmidocrank: Int,
    val ldbid: Int,
    val hmidocindate: LocalDateTime,
    val hmidocadrlist: Boolean
)

data class NewsDocAdr (
    val docadrid: Long,
    val hmidocid: Long,
    val adressid: Long,
    val docadrfile: String,
    val ldbid: Int,
    val docadrout: Boolean,
    val docadrindate: LocalDateTime,
    val docadrupdate: LocalDateTime,
)