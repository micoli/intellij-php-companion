package org.micoli.php

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.assertj.core.api.Assertions.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.micoli.php.service.DebouncedRunnable

class DebouncedRunnableTest {
    private lateinit var debouncedRunnable: DebouncedRunnable
    private var counter: AtomicInteger = AtomicInteger(0)
    private var callbackCounter: AtomicInteger = AtomicInteger(0)

    @Before
    fun setUp() {
        counter = AtomicInteger(0)
        callbackCounter = AtomicInteger(0)
    }

    @After
    fun tearDown() {
        debouncedRunnable.close()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRunDelaysExecution() {
        val delay: Long = 300
        val latch = CountDownLatch(1)

        debouncedRunnable =
            DebouncedRunnable(
                {
                    counter.incrementAndGet()
                    latch.countDown()
                },
                delay,
                null,
            )

        assertThat(counter.get()).isEqualTo(0)
        debouncedRunnable.run()
        assertThat(counter.get()).isEqualTo(0)

        assertThat(latch.await(delay + 200, TimeUnit.MILLISECONDS)).isTrue
        assertThat(counter.get()).isEqualTo(1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testMultipleRunCallsDebounce() {
        val delay: Long = 300
        val latch = CountDownLatch(1)

        debouncedRunnable =
            DebouncedRunnable(
                {
                    counter.incrementAndGet()
                    latch.countDown()
                },
                delay,
                null,
            )

        debouncedRunnable.run()
        Thread.sleep(100)
        debouncedRunnable.run()
        Thread.sleep(100)
        debouncedRunnable.run()

        assertThat(latch.await(delay + 200, TimeUnit.MILLISECONDS)).isTrue
        assertThat(counter.get()).isEqualTo(1)
    }

    @Test
    fun testExecuteNow() {
        val delay: Long = 500

        debouncedRunnable = DebouncedRunnable({ counter.incrementAndGet() }, delay, null)

        debouncedRunnable.run()
        assertThat(counter.get()).isEqualTo(0)

        debouncedRunnable.executeNow()
        assertThat(counter.get()).isEqualTo(1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testCallbackIsExecuted() {
        val delay: Long = 300
        val latch = CountDownLatch(1)

        debouncedRunnable =
            DebouncedRunnable(
                { counter.incrementAndGet() },
                delay,
                {
                    callbackCounter.incrementAndGet()
                    latch.countDown()
                },
            )

        debouncedRunnable.run()
        assertThat(latch.await(delay + 200, TimeUnit.MILLISECONDS)).isTrue

        assertThat(counter.get()).isEqualTo(1)
        assertThat(callbackCounter.get()).isEqualTo(1)
    }

    @Test
    fun testGetDelayMillis() {
        val delay: Long = 500
        debouncedRunnable = DebouncedRunnable({}, delay, null)

        assertThat(debouncedRunnable.delayMillis).isEqualTo(delay)
    }
}
