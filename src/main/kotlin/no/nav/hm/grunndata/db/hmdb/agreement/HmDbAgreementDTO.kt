package no.nav.hm.grunndata.db.hmdb.agreement

data class HmDbAgreementDTO (
    val newsDTO: NewsDTO,
    val docs: List<NewsDocDTO>,
    val poster: List<AvtalePostDTO>
)