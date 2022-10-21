package no.nav.hm.grunndata.db

import io.micronaut.runtime.Micronaut

object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("no.nav.hm.grunndata.db")
            .mainClass(Application.javaClass)
            .start()
    }
}