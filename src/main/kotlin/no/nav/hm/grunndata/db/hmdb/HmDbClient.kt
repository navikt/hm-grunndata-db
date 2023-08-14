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
import no.nav.hm.grunndata.db.hmdb.iso.IsoDTO
import no.nav.hm.grunndata.db.hmdb.product.HmDbProductBatchDTO
import no.nav.hm.grunndata.db.hmdb.product.TechDataDTO
import no.nav.hm.grunndata.db.hmdb.supplier.HmdbSupplierDTO
import no.nav.hm.grunndata.db.hmdb.techlabel.TechLabelDTO
import java.time.LocalDateTime

@Client("\${hmdb.url}")
@Headers(
    Header(name = USER_AGENT, value="Grunndata http client"),
    Header(name= ACCEPT, value = "application/json")
)
interface HmDbClient {

    @Get("/api/v1/sync/suppliers")
    fun fetchSuppliers(@QueryValue @Format("yyyy-MM-dd'T'HH:mm:ss") lastupdated: LocalDateTime): List<HmdbSupplierDTO>?

    @Get("/api/v1/sync/suppliers/all")
    fun fetchAllSuppliers(): List<HmdbSupplierDTO>?

    @Get("/api/v1/sync/products")
    fun fetchProducts(@QueryValue @Format("yyyy-MM-dd'T'HH:mm:ss") changeFrom: LocalDateTime,
                      @QueryValue @Format("yyyy-MM-dd'T'HH:mm:ss") changeTo: LocalDateTime): HmDbProductBatchDTO?

    @Get("/api/v1/sync/agreements")
    fun fetchAgreements(): List<HmDbAgreementDTO>?

    @Get("/api/v1/sync/agreements/active/ids")
    fun fetchAgreementsIdActive(): List<Long>?

    @Get("/api/v1/sync/products/{productId}")
    fun fetchProductsById(productId: Long): HmDbProductBatchDTO?

    @Get("/api/v1/sync/products/range/{artIdStart}/{artIdEnd}")
    fun fetchProductsByArtIdStartEnd(artIdStart: Long, artIdEnd: Long): HmDbProductBatchDTO?

    @Get("/api/v1/sync/products/articleId/{articleId}")
    fun fetchProductByArticleId(articleId: Long): HmDbProductBatchDTO?

    @Get("/api/v1/sync/iso")
    fun fetchIso(): List<IsoDTO>

    @Get("/api/v1/sync/products/active/ids")
    fun fetchProductsIdActive(): List<Long>?

    @Get("/api/v1/sync/techlabels")
    fun fetchAllTechlabels(): List<TechLabelDTO>

}
