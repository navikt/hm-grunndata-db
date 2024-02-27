package no.nav.hm.grunndata.db.agreement

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.db.hmdb.agreement.mergePosts
import no.nav.hm.grunndata.rapid.dto.AgreementPost
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class MergePostsTest(private val objectMapper: ObjectMapper) {

    @Test
    fun testMergePosts() {
        val oldId = UUID.randomUUID()
        val oldId2 = UUID.randomUUID()
        val newId1 = UUID.randomUUID()
        val newId2 = UUID.randomUUID()
        val newPost = AgreementPost(
            identifier = "HMDB-1",
            id = newId1,
            nr = 1,
            refNr = "1A",
            title = "Title changed",
            description = "Description changed"
        )
        val newPost2 = AgreementPost(
            identifier = "HMDB-4",
            id = newId2,
            nr = 2,
            refNr = "2A",
            title = "Title 2",
            description = "Description 2"
        )

        val oldPost1 = AgreementPost(
            identifier = "HMDB-1",
            id = oldId,
            nr = 1,
            refNr = "1A",
            title = "Title 1",
            description = "Description 1"
        )
        val oldPost2 = AgreementPost(
            identifier = "HMDB-2",
            id = oldId2,
            nr = 2,
            refNr = "2A",
            title = "Title 2",
            description = "Description 2"
        )

        val oldPost3 = AgreementPost(
            identifier = "HMDB-3",
            id = oldId2,
            nr = 3,
            refNr = "3A",
            title = "Title 3",
            description = "Description 3"
        )

        val newPosts = listOf(newPost, newPost2)
        val oldPosts = listOf(oldPost1, oldPost2, oldPost3)
        val mergedPosts = mergePosts(oldPosts, newPosts)
        mergedPosts.size shouldBe 2
        mergedPosts[0].id shouldBe oldId
        mergedPosts[0].identifier shouldBe "HMDB-1"
        mergedPosts[0].title shouldBe "Title changed"
        mergedPosts[0].description shouldBe "Description changed"
        mergedPosts[1].id shouldBe newId2
        mergedPosts[1].identifier shouldBe "HMDB-4"
        mergedPosts[1].title shouldBe "Title 2"
        println(objectMapper.writeValueAsString(mergedPosts))

    }
}