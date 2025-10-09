package org.micoli.php

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.micoli.php.builders.SymfonyProfilerConfigurationBuilder
import org.micoli.php.symfony.profiler.SymfonyProfileService

class SymfonyProfileServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/profiler"

    lateinit var symfonyProfileService: SymfonyProfileService

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()

        myFixture.copyDirectoryToProject("var", "/var")
        symfonyProfileService = SymfonyProfileService.getInstance(project)
        symfonyProfileService.loadConfiguration(
            SymfonyProfilerConfigurationBuilder.create()
                .withProfilerPath("var/cache/dev/profiler")
                .withEnabled(true)
                .build())
    }

    fun testItLoadProfileDump() {
        val symfonyProfileDTO = symfonyProfileService.elements.first()
        val profileDump = symfonyProfileService.loadProfilerDump(symfonyProfileDTO.token)
        assertEquals("8af368", profileDump?.token)
    }
}
