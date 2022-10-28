package no.nav.hm.grunndata.db.search

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.opensearch.client.RestClient
import org.opensearch.client.RestHighLevelClient
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

@Factory
class OpenSearchConfig(private val openSearchEnv: OpenSearchEnv) {

    companion object {
        private val LOG = LoggerFactory.getLogger(OpenSearchConfig::class.java)
    }
    @Singleton
    fun buildOpenSearchClient(): RestHighLevelClient {
        val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(
            AuthScope.ANY,
            UsernamePasswordCredentials(openSearchEnv.user, openSearchEnv.password)
        )
        val builder = RestClient.builder(HttpHost.create(openSearchEnv.url))
            .setHttpClientConfigCallback {
                    httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                if ("https://localhost:9200" == openSearchEnv.url && "admin" == openSearchEnv.password) {
                    LOG.info("Using dev settings for ${openSearchEnv.url}")
                    devAndTestSettings(httpClientBuilder)
                }
                httpClientBuilder
            }
        LOG.info("Opensearch client using ${openSearchEnv.user} and url ${openSearchEnv.url}")
        return RestHighLevelClient(builder)
    }

    private fun devAndTestSettings(httpClientBuilder: HttpAsyncClientBuilder) {
        httpClientBuilder.setSSLHostnameVerifier { _, _ -> true }
        val context = SSLContext.getInstance("SSL")
        context.init(null, arrayOf(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate>? {
                return null
            }

        }), SecureRandom())
        httpClientBuilder.setSSLContext(context)
    }

}

@ConfigurationProperties("opensearch")
class OpenSearchEnv {
    var user: String="admin"
    var password: String="admin"
    var url: String = "https://localhost:9200"
}
