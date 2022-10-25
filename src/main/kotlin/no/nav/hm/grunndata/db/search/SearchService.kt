package no.nav.hm.grunndata.db.search

import jakarta.inject.Singleton
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.opensearch.client.Request
import org.opensearch.client.RestHighLevelClient
import org.slf4j.LoggerFactory
import java.net.ConnectException

@Singleton
class SearchService (private val osclient: RestHighLevelClient) {

    fun searchWithBody(index: String, params: Map<String, String>?, body: String): String {
        if (params!=null) require(onlyAllowedParams(params)) { "Disallowed request params present in " + params.keys }
        return try {
            val entity = StringEntity(body, ContentType.APPLICATION_JSON)
            val request: Request = newRequest("POST", "/$index/_search", params, entity)
            val httpEntity: HttpEntity = osclient.lowLevelClient.performRequest(request).entity
            EntityUtils.toString(httpEntity)
        } catch (e: ConnectException) {
            LOG.error("No connection to Opensearch", e)
            throw e
        }
    }

    fun searchWithQuery(index: String, params: Map<String, String>?): String {
        if (params!=null) require(onlyAllowedParams(params)) { "Disallowed request params present in " + params.keys }
        return try {
            val request: Request = newRequest("GET", "/$index/_search", params, null)
            val responseEntity: HttpEntity = osclient.lowLevelClient.performRequest(request).entity
            EntityUtils.toString(responseEntity)
        } catch (e: ConnectException) {
           LOG.error("No connection to Opensearch", e)
            throw e
        }
    }

    private fun newRequest(method: String, endpoint: String, params: Map<String, String>?, entity: StringEntity? ): Request {
        val request = Request(method, endpoint)
        params?.forEach(request::addParameter)
        request.entity = entity
        return request
    }


    private fun onlyAllowedParams(params: Map<String, String>): Boolean {
        return ALLOWED_REQUEST_PARAMS.containsAll(params.keys)
    }
    companion object {
        const val PRODUCT = "product"
        const val AGREEMENT = "agreement"

        private val LOG = LoggerFactory.getLogger(SearchService::class.java)
        val ALLOWED_REQUEST_PARAMS = setOf("q", "filter_path", "pretty")
    }
}
