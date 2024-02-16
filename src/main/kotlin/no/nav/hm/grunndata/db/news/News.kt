package no.nav.hm.grunndata.db.news

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import no.nav.hm.grunndata.db.HMDB
import no.nav.hm.grunndata.rapid.dto.NewsStatus
import no.nav.hm.grunndata.rapid.dto.NewsDTO
import java.time.LocalDateTime
import java.util.*

@MappedEntity("news_v1")
data class News(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val identifier: String = UUID.randomUUID().toString(),
    val title: String,
    val text: String,
    val status: NewsStatus = NewsStatus.ACTIVE,
    val published: LocalDateTime = LocalDateTime.now(),
    val expired: LocalDateTime = LocalDateTime.now().plusMonths(3),
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val createdBy: String = HMDB,
    val updatedBy: String = HMDB,
    val author: String = "Admin",
)


fun News.toDTO(): NewsDTO = NewsDTO(
    id = id,
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

fun NewsDTO.toEntity(): News = News(
    id = id,
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