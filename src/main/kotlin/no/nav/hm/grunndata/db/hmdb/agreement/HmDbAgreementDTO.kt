package no.nav.hm.grunndata.db.hmdb.agreement

import no.nav.hm.grunndata.db.hmdb.news.HMDNewsDTO

data class HmDbAgreementDTO(
    val newsDTO: HMDNewsDTO,
    val isonumber: List<String> = emptyList(),
    val poster: List<AvtalePostDTO>,
    val newsDocHolder: List<NewsDocHolder> = emptyList()
)
