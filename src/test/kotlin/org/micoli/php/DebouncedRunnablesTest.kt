package org.micoli.php

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.assertj.core.api.Assertions.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.micoli.php.service.DebouncedRunnable
import org.micoli.php.service.DebouncedRunnables

class DebouncedRunnablesTest {
    private var debouncedRunnables: DebouncedRunnables = DebouncedRunnables()
    private var counter: AtomicInteger = AtomicInteger(0)

    @Before
    fun setUp() {
        debouncedRunnables = DebouncedRunnables()
        counter = AtomicInteger(0)
    }

    @After
    fun tearDown() {
        debouncedRunnables.reset()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRunWithName() {
        val delay: Long = 300
        val latch = CountDownLatch(1)

        val runnable =
            debouncedRunnables.run(
                {
                    counter.incrementAndGet()
                    latch.countDown()
                },
                "testRunnable",
                delay,
            )

        assertThat(runnable).isNotNull()
        assertThat(counter.get()).isEqualTo(0)

        assertThat(latch.await(delay + 200, TimeUnit.MILLISECONDS)).isTrue()
        assertThat(counter.get()).isEqualTo(1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRunReusesSameRunnableForSameName() {
        val latch = CountDownLatch(1)
        val callCounter = AtomicInteger(0)

        val runnable1 = getSameNameRunnable(callCounter, latch)
        for (i in 0..9) {
            getSameNameRunnable(callCounter, latch)
        }
        Thread.sleep(100)
        val runnable2 = getSameNameRunnable(callCounter, latch)

        assertThat(runnable2).isEqualTo(runnable1)

        assertThat(latch.await(600 + 200, TimeUnit.MILLISECONDS)).isTrue
        assertThat(callCounter.get()).isEqualTo(1)
    }

    private fun getSameNameRunnable(
        callCounter: AtomicInteger,
        latch: CountDownLatch,
        delay: Long = 600,
    ): DebouncedRunnable? =
        debouncedRunnables.run(
            {
                counter.incrementAndGet()
                callCounter.incrementAndGet()
                latch.countDown()
            },
            "sameNameRunnable",
            delay,
        )

    @Test
    @Throws(InterruptedException::class)
    fun testRunWithCallback() {
        val delay: Long = 300
        val taskLatch = CountDownLatch(1)
        val callbackLatch = CountDownLatch(1)
        val callbackCounter = AtomicInteger(0)

        debouncedRunnables.run(
            {
                counter.incrementAndGet()
                taskLatch.countDown()
            },
            "runnableWithCallback",
            delay,
            {
                callbackCounter.incrementAndGet()
                callbackLatch.countDown()
            },
        )

        assertThat(taskLatch.await(delay + 200, TimeUnit.MILLISECONDS)).isTrue
        assertThat(callbackLatch.await(100, TimeUnit.MILLISECONDS)).isTrue

        assertThat(counter.get()).isEqualTo(1)
        assertThat(callbackCounter.get()).isEqualTo(1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testMultipleDistinctRunnables() {
        val delay: Long = 300
        val latch1 = CountDownLatch(1)
        val latch2 = CountDownLatch(1)
        val counter1 = AtomicInteger(0)
        val counter2 = AtomicInteger(0)

        val runnable1 =
            debouncedRunnables.run(
                {
                    counter1.incrementAndGet()
                    latch1.countDown()
                },
                "runnable1",
                delay,
            )

        val runnable2 =
            debouncedRunnables.run(
                {
                    counter2.incrementAndGet()
                    latch2.countDown()
                },
                "runnable2",
                delay,
            )

        assertThat(runnable2).isNotEqualTo(runnable1)

        assertThat(latch1.await(delay + 200, TimeUnit.MILLISECONDS)).isTrue
        assertThat(latch2.await(100, TimeUnit.MILLISECONDS)).isTrue

        assertThat(counter1.get()).isEqualTo(1)
        assertThat(counter2.get()).isEqualTo(1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testReset() {
        val delay: Long = 400

        debouncedRunnables.run({ counter.incrementAndGet() }, "runnable1", delay)
        debouncedRunnables.run({ counter.incrementAndGet() }, "runnable2", delay)

        debouncedRunnables.reset()

        Thread.sleep(delay + 1700)

        assertThat(counter.get()).isEqualTo(0)
    }
}
