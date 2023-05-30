package no.nav.hm.grunndata.db.hmdb.product

data class ArticlePostDTO(
    val artid: Long,
    val apostid: Long,
    val postrank: Int,
    val newsid: Long?
)
