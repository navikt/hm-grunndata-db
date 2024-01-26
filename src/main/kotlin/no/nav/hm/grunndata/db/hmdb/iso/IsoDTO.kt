package no.nav.hm.grunndata.db.hmdb.iso

import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime


data class IsoDTO(
    val isocode: String,
    val isolevel: Int?=null,
    val isotitle: String?=null,
    val engisotitle: String?=null,
    val engisotext: String?=null,
    val isotext: String?=null,
    val isotextshort: String?=null,
    val statusdate: LocalDateTime,
    val isactive: Boolean?=false,
    val showtech: Boolean?=false,
    val allowmulti: Boolean?=false,
)

@Introspected
data class SearchWord(
    val isocode: String,
    val searchwordid: Long,
    val searchword: String,
    val searchwordeng: String
)

@Introspected
data class IsoSearchWord(
    val isos: List<IsoDTO>,
    val searchWords: Map<String, List<SearchWord>>
)
