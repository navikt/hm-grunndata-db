package no.nav.hm.grunndata.db.hmdb

import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.mockk
import no.nav.hm.grunndata.db.agreement.AgreementService
import no.nav.hm.grunndata.db.hmdb.product.HmDBProductMapper
import no.nav.hm.grunndata.db.hmdb.product.HmDbProductBatchDTO
import no.nav.hm.grunndata.db.hmdb.product.HmDbProductDTO
import no.nav.hm.grunndata.db.series.SeriesService
import no.nav.hm.grunndata.db.supplier.SupplierService
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class HmDbProductMapperTest(private val hmDBProductMapper: HmDBProductMapper) {

    @MockBean(SupplierService::class)
    fun mockSupplierService(): SupplierService = mockk(relaxed = true)

    @MockBean(AgreementService::class)
    fun mockAgreementService(): AgreementService = mockk(relaxed = true)

    @MockBean(SeriesService::class)
    fun mockSeriesService(): SeriesService = mockk(relaxed = true)

    @MockBean(RapidPushService::class)
    fun mockRapidService(): RapidPushService = mockk(relaxed = true)

    @Test
    fun productMapperTest() {
        val prod = HmDbProductDTO(artid = 1, adescshort = "short desc", adraft = false, aindate = LocalDateTime.now().minusMonths(6),
            achange = LocalDateTime.now().minusMonths(6), aisapproved = true, anbudid = "", aout = false, aoutdate = LocalDateTime.now().minusDays(1),
            apostid = 1, postrank = 1, artname = "", artno = "", artpostid = "", hasanbud = false, isactive = true, isocode = "",
            newsid = null, newspublish = null, newsexpire = null, prodid = 1, pchange = LocalDateTime.now().minusMonths(6),
            prodname = "", pshortdesc = "", stockid = "", supplier = "", poutdate = null)

        val batchDTO = HmDbProductBatchDTO(products = listOf(prod), articlePosts = emptyMap(),
            blobs = emptyMap(), techdata = emptyMap())
        val mapped = hmDBProductMapper.mapProduct(prod = prod, batch = batchDTO)
        mapped.attributes.shortdescription shouldBe "short desc"
        mapped.status shouldBe ProductStatus.INACTIVE

    }
}