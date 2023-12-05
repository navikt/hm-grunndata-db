package no.nav.hm.grunndata.db.media

import no.nav.hm.grunndata.rapid.dto.MediaInfo
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType
import java.time.LocalDateTime

data class Media (
    val sourceUri: String,
    val filename: String?=null,
    val uri:    String,
    val priority: Int = -1,
    val type: MediaType = MediaType.IMAGE,
    val text:   String?=null,
    val source: MediaSourceType = MediaSourceType.HMDB,
    val updated: LocalDateTime = LocalDateTime.now(),
) {
    override fun hashCode(): Int {
        return uri.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Media) return false
        return uri == other.uri
    }

}

fun MediaInfo.toEntity(): Media = Media (
    sourceUri = sourceUri, filename = filename, uri = uri, priority = priority,
    type=type, text = text, source = source, updated = updated
)

fun Media.toMediaInfo(): MediaInfo = MediaInfo (
    sourceUri = sourceUri,
    uri = uri,
    filename = filename,
    priority = priority,
    type = type,
    text = text,
    source = source,
    updated= updated
)