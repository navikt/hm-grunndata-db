package no.nav.hm.grunndata.db.hmdb

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

@MicronautTest
class BlobsTest(private val objectMapper: ObjectMapper) {

    @Test
    fun mapBlobsTest() {
        val blobs = listOf(BlobDTO(prodid = 1, "billede", "123.jpg", "1"),
            BlobDTO(prodid = 1, "billede", "123.jpg", "1"),
            BlobDTO(prodid = 2, "billede", "123_1.jpg", "1"),
            BlobDTO(prodid = 3, "bruksanvisning", "123.pdf", "1")
        )

        val media = mapBlobs(blobs)
        media.size shouldBe 3
        media.last().uri shouldBe "123.pdf"
    }
}