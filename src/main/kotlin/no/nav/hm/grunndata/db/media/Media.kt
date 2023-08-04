package no.nav.hm.grunndata.db.media

import no.nav.hm.grunndata.rapid.dto.MediaInfo
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType
import java.time.LocalDateTime

data class Media (
    val sourceUri: String,
    val uri:    String,
    val priority: Int = -1,
    val type: MediaType = MediaType.IMAGE,
    val text:   String?=null,
    val source: MediaSourceType = MediaSourceType.HMDB,
    val updated: LocalDateTime = LocalDateTime.now(),
)

fun MediaInfo.toEntity(): Media = Media (
    sourceUri = sourceUri, uri = uri, priority = priority,
    type=type, text = text, source = source, updated = updated
)

fun Media.toMediaInfo(): MediaInfo = MediaInfo (
    sourceUri = sourceUri,
    uri = uri,
    priority = priority,
    type = type,
    text = text,
    source = source,
    updated= updated
)