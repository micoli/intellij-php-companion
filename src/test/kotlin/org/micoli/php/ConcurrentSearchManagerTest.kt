package org.micoli.php

import java.time.Duration
import junit.framework.TestCase
import org.assertj.core.api.Assertions.*
import org.micoli.php.service.intellij.search.ConcurrentSearchManager

class ConcurrentSearchManagerTest : TestCase() {
    fun testItAllowToAddASearchIfIsNotPresent() {
        val concurrentSearchManager = ConcurrentSearchManager(Duration.ofSeconds(20))

        concurrentSearchManager.addSearch("test")

        assertThat(concurrentSearchManager.isSearchInProgress("test")).isTrue
        assertThat(concurrentSearchManager.isSearchInProgress("test2")).isFalse
        concurrentSearchManager.removeSearch("test")
        assertThat(concurrentSearchManager.isSearchInProgress("test")).isFalse
    }

    @Throws(InterruptedException::class)
    fun testItDisallowToAddASearchIfIsPresent() {
        val concurrentSearchManager = ConcurrentSearchManager(Duration.ofSeconds(2))
        concurrentSearchManager.addSearch("test")
        assertThat(concurrentSearchManager.isSearchInProgress("test")).isTrue
        Thread.sleep(3000)
        assertThat(concurrentSearchManager.isSearchInProgress("test")).isFalse
        assertThat(concurrentSearchManager.isEmpty()).isTrue
    }
}
