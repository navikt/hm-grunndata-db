package no.nav.hm.grunndata.db.hmdb.product

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.LocalDateTime

data class BlobDTO (
    val prodid: Long,
    val blobtype: String,
    val blobfile: String,
    val blobuse: String,
    val statusdate: LocalDateTime? = null
)
