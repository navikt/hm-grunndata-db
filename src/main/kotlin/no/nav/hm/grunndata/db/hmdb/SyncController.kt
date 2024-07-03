package no.nav.hm.grunndata.db.hmdb

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Put
import no.nav.hm.grunndata.db.hmdb.agreement.AgreementSync
import no.nav.hm.grunndata.db.hmdb.iso.IsoSync
import no.nav.hm.grunndata.db.hmdb.news.NewsSync
import no.nav.hm.grunndata.db.hmdb.product.ProductSync
import no.nav.hm.grunndata.db.hmdb.techlabel.TechLabelSync
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Controller("/internal/sync")
class SyncController(private val productSync: ProductSync,
                     private val hmDbBatchRepository: HmDbBatchRepository,
                     private val agreementSync: AgreementSync,
                     private val newsSync: NewsSync,
                     private val isoSync: IsoSync,
                     private val techLabelSync: TechLabelSync
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SyncController::class.java)
    }

    @Get("/agreements")
    suspend fun syncAgreements() {
        LOG.info("call sync agreements from HMDB")
        agreementSync.syncAgreements()
    }

    @Get("/agreements/fixDb")
    suspend fun fixAgreementsInDb(){
        LOG.info("call fix agreements in db")
        agreementSync.fixAgreementsInDb()
    }

    @Get("/news")
    suspend fun syncNews() {
        LOG.info("call sync news from HMDB")
        newsSync.syncNews()
    }

    @Get("/products")
    suspend fun syncProducts() {
        LOG.info("call sync products from HMDB")
        productSync.syncProducts()
    }


    @Get("/iso")
    suspend fun syncIso() {
        LOG.info("call sync iso from HMDB")
        isoSync.syncIsoWithSearchWords()
    }

    @Get("/products/{productId}")
    suspend fun syncProductsById(productId:Long) { // seriesId in HMDB
        LOG.info("call sync products from HDMB for $productId")
        productSync.syncProductsById(productId)
    }

    @Get("/products/range/{artIdStart}/{artIdEnd}")
    suspend fun syncProductsByArtIdStartEnd(artIdStart:Long, artIdEnd: Long){
        LOG.info("call sync products from articleId start $artIdStart end $artIdEnd")
        productSync.syncProductsByArtIdStartEnd(artIdStart, artIdEnd)
    }

    @Get("/products/all")
    suspend fun syncAllProducts() {
        LOG.info("Call sync all products")
        productSync.syncAllActiveProducts()
        LOG.info("Sync all finished")
    }


    @Get("/products/states")
    suspend fun syncProductStates() {
        LOG.info("Call sync product states")
        productSync.syncHMDBDeletedProductStates()
        LOG.info("Sync product states finished")
    }


    @Put("/products/syncFrom/{syncFrom}")
    suspend fun setProductsSyncFrom(syncFrom: LocalDateTime) {
        LOG.info("Reset syncfrom for products to $syncFrom")
        hmDbBatchRepository.findByName(SYNC_PRODUCTS)?.let {
            hmDbBatchRepository.update(it.copy(syncfrom=syncFrom))
        }
    }

    @Put("/suppliers/syncFrom/{syncFrom}")
    suspend fun setSuppliersSyncFrom(syncFrom: LocalDateTime) {
        LOG.info("Reset syncfrom for suppliers to $syncFrom")
        hmDbBatchRepository.findByName(SYNC_SUPPLIERS)?.let {
            hmDbBatchRepository.update(it.copy(syncfrom=syncFrom))
        }
    }

    @Put("/agreements/syncFrom/{syncFrom}")
    suspend fun setAgreementsSyncFrom(syncFrom: LocalDateTime) {
        LOG.info("Reset syncfrom for agreements to $syncFrom")
        hmDbBatchRepository.findByName(SYNC_AGREEMENTS)?.let {
            hmDbBatchRepository.update(it.copy(syncfrom=syncFrom))
        }
    }

    @Get("/techlabels/all")
    suspend fun syncAllTechLabels() {
        LOG.info("Call sync all tech labels")
        techLabelSync.syncAllTechLabels()
    }

}
