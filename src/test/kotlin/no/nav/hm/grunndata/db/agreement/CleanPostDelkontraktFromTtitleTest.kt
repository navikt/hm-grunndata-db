package no.nav.hm.grunndata.db.agreement

import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.db.hmdb.agreement.cleanPostTitle
import org.junit.jupiter.api.Test

@MicronautTest
class CleanPostDelkontraktFromTtitleTest {

    @Test
    fun cleanPostDelkontraktFromTitle() {
        val cleanTitle = "1. Rullator - innendørs bruk med smal bredde"
        val withPost = "Post 1: Arbeidsstol med manuelt seteløft uten seterotasjon"
        val withDelkontrakt = "Delkontrakt 1: Arbeidsstol med manuelt seteløft uten seterotasjon"

        val cleanPostDelkontraktFromTitle = cleanPostTitle(cleanTitle)
        val cleanPostDelkontraktFromWithPost = cleanPostTitle(withPost)
        val cleanPostDelkontraktFromWithDelkontrakt = cleanPostTitle(withDelkontrakt)
        cleanPostDelkontraktFromWithDelkontrakt shouldBe "1: Arbeidsstol med manuelt seteløft uten seterotasjon"
        cleanPostDelkontraktFromWithPost shouldBe "1: Arbeidsstol med manuelt seteløft uten seterotasjon"
        cleanPostDelkontraktFromTitle shouldBe "1. Rullator - innendørs bruk med smal bredde"
    }
}