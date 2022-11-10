package no.nav.hm.grunndata.db.hmdb

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

data class BlobDTO (
    val prodid: Long,
    val blobtype: String,
    val blobfile: String,
    val blobuse: String,
)
