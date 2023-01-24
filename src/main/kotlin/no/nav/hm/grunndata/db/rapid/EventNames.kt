package no.nav.hm.grunndata.db.rapid

import no.nav.hm.grunndata.db.appName

class EventNames {

    companion object {
        const val hmdbagreementsync = "$appName-hmdb-agreement-sync"
        const val hmdbsuppliersync = "$appName-hmdb-supplier-sync"
        const val hmdbproductsync = "$appName-hmdb-product-sync"
    }
}