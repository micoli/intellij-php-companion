package org.micoli.php.symfony.profiler

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBus
import com.jetbrains.rd.util.string.printToString
import com.opencsv.CSVReader
import com.opencsv.exceptions.CsvException
import java.io.IOException
import java.nio.file.FileSystems
import java.util.Map
import org.micoli.php.service.DebouncedRunnables
import org.micoli.php.service.PhpGzDecoder
import org.micoli.php.service.filesystem.FileListener
import org.micoli.php.service.filesystem.PathUtil
import org.micoli.php.service.filesystem.WatchEvent
import org.micoli.php.service.filesystem.Watchee
import org.micoli.php.service.serialize.JsonTransformer
import org.micoli.php.service.serialize.PhpUnserializer
import org.micoli.php.symfony.profiler.configuration.DataExportMode
import org.micoli.php.symfony.profiler.configuration.SymfonyProfilerConfiguration
import org.micoli.php.symfony.profiler.dataExport.CliDataExport
import org.micoli.php.symfony.profiler.dataExport.HttpDataExport
import org.micoli.php.symfony.profiler.models.PHPProfilerDump

@Service(Service.Level.PROJECT)
class SymfonyProfileService(val project: Project) : FileListener.VfsHandler<String> {
    private lateinit var exclusionFilters: List<Regex>
    private val messageBus: MessageBus = project.messageBus
    private var debouncedRunnables: DebouncedRunnables = DebouncedRunnables()

    var configuration: SymfonyProfilerConfiguration? = null
    val profilerIndexListener = FileListener<String>(this, project.messageBus)

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
                        var url = record[3]
                        if (isExcluded(url)) {
                            continue
                        }
                        configuration?.urlRoots?.forEach { urlRoot ->
                            url = url.replaceFirst(urlRoot, "")
                        }
                        elements.add(
                            SymfonyProfileDTO(
                                record[0] ?: "",
                                record[1],
                                record[2],
                                url,
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

    private fun isExcluded(url: String): Boolean {
        for (exclusionFiler in exclusionFilters) {
            if (exclusionFiler.containsMatchIn(url)) {
                return true
            }
        }
        return false
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
        this.exclusionFilters = configuration.excludeFilter.map { Regex(it) }
        profilerIndexListener.setPatterns(
            Map.of(
                "profilerIndex",
                Watchee(
                    listOf(
                        FileSystems.getDefault()
                            .getPathMatcher(
                                "glob:**" +
                                    configuration.profilerProjectPath +
                                    configuration.profilerPath +
                                    "index.csv")),
                    WatchEvent.all())))
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

    fun setAutoRefresh(autoRefresh: Boolean) {
        profilerIndexListener.isEnabled = autoRefresh
        if (autoRefresh) {
            dispatchIndexUpdated()
        }
        println(
            "!! " +
                profilerIndexListener
                    .getPatterns()
                    .map {
                        it.value.events.joinToString("/") { s -> s.name } +
                            "::" +
                            it.value.pathMatchers.map { s -> s.printToString() }
                    }
                    .joinToString(","))
    }

    fun <T> loadProfilerDumpPage(
        targetClass: Class<T>,
        token: String,
        logCallback: (log: String) -> Unit,
        errorCallback: (String) -> Unit,
        callback: (T?) -> Unit
    ) {
        logCallback("Start loading")
        (when (configuration?.profilerDataExportMode ?: DataExportMode.HTTP) {
                DataExportMode.CLI -> CliDataExport(project)
                DataExportMode.HTTP -> HttpDataExport()
            })
            .loadProfilerDumpPage(
                configuration,
                targetClass,
                token,
                logCallback,
                errorCallback,
                callback,
            )
    }

    override fun vfsHandle(id: String, file: VirtualFile) {
        dispatchIndexUpdated()
    }

    private fun dispatchIndexUpdated() {
        debouncedRunnables.run(
            { messageBus.syncPublisher(ProfilerEvents.INDEX_UPDATED).indexUpdated() },
            "refreshProfilerIndex",
            400)
    }

    fun cleanProfiles() {
        val path = profilerPath ?: return
        WriteAction.run<IOException?> { path.delete(this) }
        dispatchIndexUpdated()
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
