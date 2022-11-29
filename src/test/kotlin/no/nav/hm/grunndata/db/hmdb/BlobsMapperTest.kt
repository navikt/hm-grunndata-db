package no.nav.hm.grunndata.db.hmdb

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.db.hmdb.product.BlobDTO
import no.nav.hm.grunndata.db.hmdb.product.HmDBProductMapper
import org.junit.jupiter.api.Test


@MicronautTest
class BlobsMapperTest(private val productMapper: HmDBProductMapper, private val objectMapper: ObjectMapper) {

    @Test
    fun blobsTest() {
        val blobs = listOf(
            BlobDTO(prodid =1L, blobtype = "billede", blobfile = "123.jpg", blobuse = "1"),
            BlobDTO(prodid =1L, blobtype = "billede", blobfile = "123_3.jpg", blobuse = "1"),
            BlobDTO(prodid =1L, blobtype = "billede", blobfile = "123_1.jpg", blobuse = "1"),
            BlobDTO(prodid =1L, blobtype = "billede", blobfile = "123_1.jpg", blobuse = "2"),
            BlobDTO(prodid =1L, blobtype = "billede", blobfile = "123_2.jpg", blobuse = "1"),
            BlobDTO(prodid =1L, blobtype = "bruksanvisning", blobfile = "123.pdf", blobuse = "2"))
        val media = productMapper.mapBlobs(blobs)
        println(objectMapper.writeValueAsString(media))
    }
}