package org.micoli.php.symfony.profiler.dataExport

import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.collections.get
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jdom2.input.SAXBuilder
import org.jsoup.Jsoup
import org.micoli.php.symfony.profiler.configuration.SymfonyProfilerConfiguration
import org.micoli.php.symfony.profiler.htmlParsers.DbHtmlParser
import org.micoli.php.symfony.profiler.htmlParsers.LoggerHtmlParser
import org.micoli.php.symfony.profiler.htmlParsers.MessengerHtmlParser
import org.micoli.php.symfony.profiler.htmlParsers.RequestHtmlParser

class HttpDataExport : AbstractDataExport() {
    val saxBuilder = SAXBuilder()
    val parsers =
        listOf(
            LoggerHtmlParser(),
            DbHtmlParser(),
            MessengerHtmlParser(),
            RequestHtmlParser(),
        )

    override fun <T> loadProfilerDumpPage(
        configuration: SymfonyProfilerConfiguration?,
        targetClass: Class<T>,
        token: String,
        logCallback: (log: String) -> Unit,
        errorCallback: (String) -> Unit,
        callback: (T?) -> Unit
    ) {
        val page = pages[targetClass] ?: return
        logCallback("Start loading")
        val url =
            (((configuration?.profilerUrlRoot ?: return) + token).toHttpUrlOrNull() ?: return)
                .newBuilder()
                .addQueryParameter("panel", page)
                .addQueryParameter("type", "request")
                .build()
        try {
            createHttpClient(true, errorCallback)
                .newCall(Request.Builder().url(url).build())
                .execute()
                .use {
                    if (it.isSuccessful) {
                        logCallback("Parsing")
                        try {
                            val result = loadProfilerPage(targetClass, it.body?.string() ?: return)
                            logCallback("Parsing done")
                            callback(result)
                        } catch (e: Exception) {
                            errorCallback(e.localizedMessage)
                        }
                        return
                    }
                    errorCallback(it.message)
                }
        } catch (e: Exception) {
            errorCallback(e.localizedMessage)
        }
    }

    fun createHttpClient(
        disableSSLVerification: Boolean,
        errorCallback: (String) -> Unit
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()

        if (disableSSLVerification) {
            val trustAllCerts =
                arrayOf<TrustManager>(
                    object : X509TrustManager {
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                        ) {}

                        override fun checkServerTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                        ) {}

                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            builder
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
        }

        return builder
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .addNetworkInterceptor {
                val response = it.proceed(it.request())
                if (!response.isSuccessful) {
                    errorCallback("HTTP Error: ${response.code} - ${response.message}")
                }
                response
            }
            .build()
    }

    fun <T> loadProfilerPage(targetClass: Class<T>, htmlContent: String): T {
        val doc = Jsoup.parse(htmlContent)

        doc.select("script, style, meta, link, noscript").remove()
        doc.outputSettings().apply {
            indentAmount(2)
            prettyPrint(true)
            syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml)
        }

        @Suppress("UNCHECKED_CAST")
        try {
            for (parser in parsers) {
                if (parser.getTargetClass() == targetClass) {
                    return parser.parse(
                        saxBuilder.build(
                            doc.outerHtml().trimIndent().trimStart().byteInputStream())) as T
                }
            }
            throw Exception(
                "Failed to parse profiler page. Unable to find a parser for {${targetClass.simpleName}}")
        } catch (e: Exception) {
            throw Exception("Failed to parse profiler page. ${e.localizedMessage}", e)
        }
    }
}
