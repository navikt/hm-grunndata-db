package no.nav.hm.grunndata.db.legacy

data class ErstattProdukterDTO(
    val produkter: Iterable<ProduktDTO>,
    val tekniskeData: Iterable<TekniskeDataDTO>,
)
