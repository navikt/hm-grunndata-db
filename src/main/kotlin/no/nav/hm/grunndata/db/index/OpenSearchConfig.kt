package no.nav.hm.grunndata.db.index

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import org.apache.hc.client5.http.auth.AuthScope
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.ssl.SSLContextBuilder
import org.opensearch.client.json.jackson.JacksonJsonpMapper
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.transport.OpenSearchTransport
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder
import org.slf4j.LoggerFactory


@Factory
class OpenSearchConfig(private val openSearchEnv: OpenSearchEnv, private val objectMapper: ObjectMapper) {

    companion object {
        private val LOG = LoggerFactory.getLogger(OpenSearchConfig::class.java)
    }

    @Singleton
    fun buildOpenSearchClient(): OpenSearchClient {
        val host = HttpHost.create(openSearchEnv.url)
        val credentialsProvider: BasicCredentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(
            AuthScope(host),
            UsernamePasswordCredentials(openSearchEnv.user, openSearchEnv.password.toCharArray())
        )
        val sslcontext = if ("https://localhost:9200" == openSearchEnv.url && "admin" == openSearchEnv.user) {
            LOG.warn("Using dev/test sslcontext cause url is ${openSearchEnv.url} and user is ${openSearchEnv.user}")
            SSLContextBuilder
                .create()
                .loadTrustMaterial(
                    null
                ) { chains: Array<X509Certificate?>?, authType: String? -> true }
                .build()
             } else {
                SSLContext.getDefault()
             }

        val builder = ApacheHttpClient5TransportBuilder.builder(host)
            .setMapper(JacksonJsonpMapper(objectMapper))
            .setHttpClientConfigCallback { httpClientBuilder: HttpAsyncClientBuilder ->
                val tlsStrategy = ClientTlsStrategyBuilder.create()
                    .setSslContext(sslcontext)
                    .build()
                val connectionManager = PoolingAsyncClientConnectionManagerBuilder
                    .create()
                    .setTlsStrategy(tlsStrategy)
                    .build()
                httpClientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setConnectionManager(connectionManager)
            }

        val transport: OpenSearchTransport = builder.build()
        val client = OpenSearchClient(transport)
        LOG.info("Opensearch client using ${openSearchEnv.user} and url ${openSearchEnv.url}")
        return client
    }


}

@ConfigurationProperties("opensearch")
class OpenSearchEnv {
    var user: String = "admin"
    var password: String = "admin"
    var url: String = "https://localhost:9200"
}
