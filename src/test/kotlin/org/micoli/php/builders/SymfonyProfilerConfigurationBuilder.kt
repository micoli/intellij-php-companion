package org.micoli.php.builders

import org.micoli.php.symfony.profiler.configuration.SymfonyProfilerConfiguration

class SymfonyProfilerConfigurationBuilder {
    private val script: SymfonyProfilerConfiguration = SymfonyProfilerConfiguration()

    fun withEnabled(enabled: Boolean): SymfonyProfilerConfigurationBuilder {
        script.enabled = enabled
        return this
    }

    fun withProfilerPath(profilerPath: String): SymfonyProfilerConfigurationBuilder {
        script.profilerPath = profilerPath
        return this
    }

    fun withProfilerUrlRoot(profilerUrlRoot: String): SymfonyProfilerConfigurationBuilder {
        script.profilerUrlRoot = profilerUrlRoot
        return this
    }

    fun build(): SymfonyProfilerConfiguration = script

    companion object {
        @JvmStatic
        fun create(): SymfonyProfilerConfigurationBuilder = SymfonyProfilerConfigurationBuilder()
    }
}
