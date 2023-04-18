package no.nav.hm.grunndata.db.hmdb.iso

import java.time.LocalDateTime


data class IsoDTO(
    val isocode: String?=null,
    val isolevel: Int?=null,
    val isotitle: String?=null,
    val engisotitle: String?=null,
    val engisotext: String?=null,
    val isotext: String?=null,
    val statusdate: LocalDateTime,
    val isactive: Boolean?=false,
    val showtech: Boolean?=false,
    val allowmulti: Boolean?=false,
)
