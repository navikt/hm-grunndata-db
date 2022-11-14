package no.nav.hm.grunndata.db.hmdb

data class HmDbAgreementDTO (
    val newsDTO: NewsDTO,
    val poster: List<AvtalePostDTO>
)