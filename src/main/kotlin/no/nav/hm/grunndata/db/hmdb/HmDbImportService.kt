package no.nav.hm.grunndata.db.hmdb

import jakarta.inject.Singleton

@Singleton
class HmDbImportService(private val hmDbLeverandorerBatchRepository: HmDbLeverandorerBatchRepository) {

    suspend fun updateSuppliers() {
        val supplierBatch = hmDbLeverandorerBatchRepository.findFirstOrderByCreatedDesc()
        val suppliers = supplierBatch.toSupplierList()
    }
}
