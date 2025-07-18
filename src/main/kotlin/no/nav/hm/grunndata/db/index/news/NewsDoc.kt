package no.nav.hm.grunndata.db.index.news

import no.nav.hm.grunndata.db.index.SearchDoc
import no.nav.hm.grunndata.rapid.dto.NewsDTO
import no.nav.hm.grunndata.rapid.dto.NewsStatus
import java.time.LocalDateTime

data class NewsDoc (
    override val id: String,
    val identifier: String,
    val title: String,
    val text: String,
    val status: NewsStatus,
    val published: LocalDateTime,
    val expired: LocalDateTime,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val createdBy: String,
    val updatedBy: String,
    val author: String
): SearchDoc

fun NewsDTO.toDoc(): NewsDoc = NewsDoc(
    id = id.toString(),
    identifier = identifier,
    title = title,
    text = text,
    status = status,
    published = published,
    expired = expired,
    created = created,
    updated = updated,
    createdBy = createdBy,
    updatedBy = updatedBy,
    author = author
)