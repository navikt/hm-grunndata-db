package no.nav.hm.grunndata.db.hmdb

import io.micronaut.core.convert.format.Format
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Headers
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.HttpHeaders.USER_AGENT
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import no.nav.hm.grunndata.db.hmdb.agreement.HmDbAgreementDTO
import no.nav.hm.grunndata.db.hmdb.news.HMDNewsDTO
import no.nav.hm.grunndata.db.hmdb.iso.IsoDTO
import no.nav.hm.grunndata.db.hmdb.iso.IsoSearchWord
import no.nav.hm.grunndata.db.hmdb.product.HmDbProductBatchDTO
import no.nav.hm.grunndata.db.hmdb.supplier.HmdbSupplierDTO
import no.nav.hm.grunndata.db.hmdb.techlabel.HmdbTechLabelDTO
import java.time.LocalDateTime

@Client("\${hmdb.url}")
@Headers(
    Header(name = USER_AGENT, value="Grunndata http client"),
    Header(name= ACCEPT, value = "application/json")
)
interface HmDbClient {

    @Get("/api/v1/sync/suppliers")
    suspend fun fetchSuppliers(@QueryValue @Format("yyyy-MM-dd'T'HH:mm:ss") lastupdated: LocalDateTime): List<HmdbSupplierDTO>?

    @Get("/api/v1/sync/suppliers/all")
    suspend fun fetchAllSuppliers(): List<HmdbSupplierDTO>?

    @Get("/api/v1/sync/products")
    suspend fun fetchProducts(@QueryValue @Format("yyyy-MM-dd'T'HH:mm:ss") changeFrom: LocalDateTime,
                      @QueryValue @Format("yyyy-MM-dd'T'HH:mm:ss") changeTo: LocalDateTime): HmDbProductBatchDTO?

    @Get("/api/v1/sync/products/series/change")
    suspend fun fetchSeries(@QueryValue @Format("yyyy-MM-dd'T'HH:mm:ss") changeFrom: LocalDateTime,
                    @QueryValue @Format("yyyy-MM-dd'T'HH:mm:ss") changeTo: LocalDateTime): HmDbProductBatchDTO?

    @Get("/api/v1/sync/agreements")
    suspend fun fetchAgreements(): List<HmDbAgreementDTO>?

    @Get("/api/v1/sync/agreements/active/ids")
    suspend fun fetchAgreementsIdActive(): List<Long>?

    @Get("/api/v1/sync/products/{productId}")
    suspend fun fetchProductsById(productId: Long): HmDbProductBatchDTO?

    @Get("/api/v1/sync/products/range/{artIdStart}/{artIdEnd}")
    suspend fun fetchProductsByArtIdStartEnd(artIdStart: Long, artIdEnd: Long): HmDbProductBatchDTO?

    @Get("/api/v1/sync/products/articleId/{articleId}")
    suspend fun fetchProductByArticleId(articleId: Long): HmDbProductBatchDTO?

    @Get("/api/v1/sync/iso")
    suspend fun fetchIso(): List<IsoDTO>

    @Get("/api/v1/sync/products/active/ids")
    suspend fun fetchProductsIdActive(): List<Long>?

    @Get("/api/v1/sync/techlabels")
    suspend fun fetchAllTechlabels(): List<HmdbTechLabelDTO>

    @Get("/api/v1/sync/iso/searchwords")
    suspend fun fetchIsoSearchwords(): IsoSearchWord
    @Get("/api/v1/sync/news")
    suspend fun fetchNews(): List<HMDNewsDTO>

}
