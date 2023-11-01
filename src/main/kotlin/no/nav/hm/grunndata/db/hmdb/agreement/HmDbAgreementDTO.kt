package no.nav.hm.grunndata.db.hmdb.agreement

data class HmDbAgreementDTO(
    val newsDTO: NewsDTO,
    val isonumber: List<String> = emptyList(),
    val poster: List<AvtalePostDTO>,
    val newsDocHolder: List<NewsDocHolder> = emptyList()
)
