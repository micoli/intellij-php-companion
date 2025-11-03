package org.micoli.php.runner

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.php.config.interpreters.PhpInterpreter
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.text.isEmpty
import kotlin.text.lowercase
import kotlin.text.trim
import org.micoli.php.runner.configuration.PhpRunnerConfiguration
import org.micoli.php.service.filesystem.PathUtil

@Service(Service.Level.PROJECT)
class PhpRunnerService(private val project: Project) {
    private var configuration: PhpRunnerConfiguration = PhpRunnerConfiguration()

    fun loadConfiguration(phpRunnerConfiguration: PhpRunnerConfiguration?) {
        if (phpRunnerConfiguration == null) {
            return
        }
        configuration = phpRunnerConfiguration
    }

    fun executePhpViaStdin(phpCode: String, stdin: String, args: Array<String>): String {
        val process =
            ProcessBuilder(
                    buildList {
                        add(configuration.bin)
                        add("-r")
                        add(phpCode)
                        addAll(args)
                    })
                .directory(PathUtil.getBaseDir(project)?.toNioPath()?.toFile())
                .redirectErrorStream(true)
                .start()

        OutputStreamWriter(process.outputStream).use { writer ->
            writer.write(stdin)
            writer.flush()
        }

        val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }

        process.waitFor()
        return output
    }

    private fun findPhpExecutable(): String? {
        val phpPath = System.getProperty("php.executable.path")
        if (phpPath != null && isValidPhpExecutable(phpPath)) {
            return phpPath
        }

        val phpHome = System.getenv("PHP_HOME")
        if (phpHome != null) {
            val phpInHome = phpHome + File.separator + "php" + (if (this.isWindows) ".exe" else "")
            if (isValidPhpExecutable(phpInHome)) {
                return phpInHome
            }
        }

        val phpFromPath = findPhpInSystemPath()
        if (phpFromPath != null && isValidPhpExecutable(phpFromPath)) {
            return phpFromPath
        }

        return null
    }

    private fun findPhpInSystemPath(): String? {
        val pathEnv = System.getenv("PATH") ?: return null

        val pathDirs: Array<String?> =
            pathEnv
                .split(File.pathSeparator.toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
        val phpExecutableName = "php" + (if (this.isWindows) ".exe" else "")

        for (pathDir in pathDirs) {
            val phpFile = File(pathDir, phpExecutableName)
            if (phpFile.exists() && phpFile.canExecute()) {
                return phpFile.absolutePath
            }
        }

        if (!this.isWindows) {
            try {
                val process = ProcessBuilder("which", "php").start()
                process.waitFor()

                if (process.exitValue() == 0) {
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val result = reader.readLine()
                    reader.close()

                    if (result != null && !result.trim { it <= ' ' }.isEmpty()) {
                        return result.trim { it <= ' ' }
                    }
                }
            } catch (_: Exception) {}
        }

        return null
    }

    private fun isValidPhpExecutable(path: String?): Boolean {
        if (path == null || path.trim { it <= ' ' }.isEmpty()) {
            return false
        }

        val phpFile = File(path)
        if (!phpFile.exists() || !phpFile.canExecute()) {
            return false
        }

        try {
            val pb = ProcessBuilder(path, "--version")
            pb.redirectErrorStream(true)
            val process = pb.start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val firstLine = reader.readLine()
            reader.close()

            val finished = process.waitFor(5, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return false
            }

            return process.exitValue() == 0 &&
                firstLine != null &&
                firstLine.lowercase(Locale.getDefault()).contains("php")
        } catch (_: Exception) {
            return false
        }
    }

    private val isWindows: Boolean
        get() = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows")

    private val isMac: Boolean
        get() {
            val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
            return osName.contains("mac") || osName.contains("darwin")
        }

    private fun findSystemPhpInterpreter(): PhpInterpreter? {
        val phpPath = findPhpExecutable()
        if (phpPath != null) {
            val interpreter = PhpInterpreter()
            interpreter.name = "System PHP"
            interpreter.homePath = phpPath
            return interpreter
        }
        return null
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PhpRunnerService {
            return project.getService(PhpRunnerService::class.java)
        }
    }
}
