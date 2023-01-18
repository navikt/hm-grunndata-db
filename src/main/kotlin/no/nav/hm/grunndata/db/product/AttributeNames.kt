package no.nav.hm.grunndata.db.product

import java.util.*

enum class AttributeNames(private val type: AttributeType) {

    manufacturer(AttributeType.STRING),
    articlename(AttributeType.STRING),
    compatibilty(AttributeType.LIST),
    keywords(AttributeType.LIST),
    shortdescription(AttributeType.HTML),
    text(AttributeType.STRING),
    url(AttributeType.URL)

}

enum class AttributeType {
    STRING, HTML, URL, LIST, JSON
}

inline fun <reified K: Enum<K>, V> enumMapOf(vararg pairs: Pair<K, V>): EnumMap<K, V> =
    pairs.toMap(EnumMap<K, V>(K::class.java))
