package no.nav.hm.grunndata.db.hmdb

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Value
import io.micronaut.core.convert.format.Format
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Headers
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.HttpHeaders.USER_AGENT
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import java.time.LocalDateTime

@Client("\${hmdb.url}")
@Headers(
    Header(name = USER_AGENT, value="Grunndata http client"),
    Header(name= ACCEPT, value = "application/json")
)
interface HmDbClient {

    @Get("/api/v1/sync/suppliers")
    fun fetchSuppliers(@QueryValue @Format("yyyy-MM-dd'T'HH:mm:ss") lastupdated: LocalDateTime): List<SupplierDTO>

    @Get("/api/v1/sync/products")
    fun fetchProducts(@QueryValue @Format("yyyy-MM-dd'T'HH:mm:ss") change: LocalDateTime): List<ProductBatchDTO>

    @Get("/api/v1/sync/news")
    fun fetchAllNews(): List<NewsDTO>

}
