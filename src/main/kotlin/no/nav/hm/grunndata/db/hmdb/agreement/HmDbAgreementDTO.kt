package no.nav.hm.grunndata.db.hmdb.agreement

data class HmDbAgreementDTO (
    val newsDTO: NewsDTO,
    val poster: List<AvtalePostDTO>
)