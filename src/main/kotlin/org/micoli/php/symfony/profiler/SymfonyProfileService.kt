package org.micoli.php.symfony.profiler

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.opencsv.CSVReader
import com.opencsv.exceptions.CsvException
import java.io.IOException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.micoli.php.service.PhpGzDecoder
import org.micoli.php.service.filesystem.PathUtil
import org.micoli.php.service.serialize.JsonTransformer
import org.micoli.php.service.serialize.PhpUnserializer
import org.micoli.php.symfony.profiler.configuration.SymfonyProfilerConfiguration
import org.micoli.php.symfony.profiler.models.PHPProfilerDump

// TODO add a purge button

@Service(Service.Level.PROJECT)
class SymfonyProfileService(val project: Project) {
    private var configuration: SymfonyProfilerConfiguration? = null

    val elements: MutableList<SymfonyProfileDTO>
        get() {
            val elements: MutableList<SymfonyProfileDTO> = ArrayList()
            if (csvProfiler == null) {
                return elements
            }
            try {
                CSVReader(csvProfiler!!.contentsToByteArray().inputStream().reader()).use {
                    val records = it.readAll()
                    for (record in records) {
                        if (record.size != 8) {
                            continue
                        }
                        elements.add(
                            SymfonyProfileDTO(
                                record[0] ?: "",
                                record[1],
                                record[2],
                                record[3].replaceFirst(configuration?.urlRoot ?: "", ""),
                                record[4],
                                record[5],
                                record[6],
                                record[7],
                                (configuration?.profilerUrlRoot ?: "") + record[0],
                            ))
                    }
                }
            } catch (_: IOException) {
                return elements
            } catch (_: CsvException) {
                return elements
            }
            return elements
        }

    private val csvProfiler: VirtualFile?
        get() {
            if (configuration == null) {
                return null
            }
            val relativePath = profilerPath?.findFileByRelativePath("index.csv")
            if (relativePath?.exists() ?: false) {
                return relativePath
            }
            return null
        }

    private val profilerPath: VirtualFile?
        get() {
            if (configuration == null) {
                return null
            }
            val pathname = configuration?.profilerPath ?: ""
            val relativePath = PathUtil.getBaseDir(project)?.findFileByRelativePath(pathname)
            if (relativePath?.exists() ?: false) {
                return relativePath
            }
            return null
        }

    fun loadConfiguration(configuration: SymfonyProfilerConfiguration?) {
        if (configuration == null) {
            return
        }
        if (!configuration.enabled) {
            return
        }
        this.configuration = configuration
    }

    fun loadProfilerDump(token: String): PHPProfilerDump? {
        if (profilerPath == null) {
            return null
        }
        val pathname =
            profilerPath!!.findFileByRelativePath(
                String.format("%s/%s/%s", token.substring(4, 6), token.substring(2, 4), token))
        if (pathname?.exists() ?: false) {
            return unserializeProfileDump(PhpGzDecoder.gzdecode(pathname.contentsToByteArray()))
        }
        return null
    }

    fun <T> loadProfilerDumpPage(
        targetClass: Class<T>,
        token: String,
        logCallback: (log: String) -> Unit,
        errorCallback: (String) -> Unit,
        callback: (T?) -> Unit
    ) {
        val page = ProfilerParser().parsers[targetClass]?.getPage() ?: return
        logCallback("Start loading")
        val url =
            ((configuration?.profilerUrlRoot ?: return) + token)
                .toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("panel", page)
                ?.build()

        val request = Request.Builder().url(url!!).build()
        try {
            createHttpClient(true, errorCallback).newCall(request).execute().use {
                if (it.isSuccessful) {
                    val body = it.body?.string() ?: return
                    logCallback("Parsing")
                    try {
                        val result = ProfilerParser().loadProfilerPage(targetClass, body)
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

    companion object {
        fun getInstance(project: Project): SymfonyProfileService {
            return project.getService(SymfonyProfileService::class.java)
        }

        fun unserializeProfileDump(content: ByteArray?): PHPProfilerDump {
            val mapper = ObjectMapper()
            val arrayNode = mapper.createArrayNode().add(mapper.createObjectNode().put("1", 1))
            val jsonTransformer =
                JsonTransformer(
                    { node ->
                        when {
                            node.isObject &&
                                node.has("data") &&
                                node["data"].isObject &&
                                node["data"].has("data") &&
                                node["data"]["data"].isObject -> node["data"]["data"]
                            else -> node
                        }
                    },
                    { node, _ ->
                        when {
                            node == arrayNode -> false
                            else -> true
                        }
                    })
            return PhpUnserializer.unserializeTo(
                content, PHPProfilerDump::class.java, jsonTransformer)
        }
    }
}
