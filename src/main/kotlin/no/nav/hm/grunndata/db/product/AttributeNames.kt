package no.nav.hm.grunndata.db.product

enum class AttributeNames(private val type: AttributeType) {

    manufacturer(AttributeType.STRING),
    articlename(AttributeType.STRING),
    compatibilty(AttributeType.JSON),
    keywords(AttributeType.JSON),
    shortdescription(AttributeType.HTML),
    text(AttributeType.STRING),
    url(AttributeType.URL)

}

enum class AttributeType {
    STRING, HTML, URL, JSON
}