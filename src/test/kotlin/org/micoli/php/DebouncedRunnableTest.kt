package org.micoli.php

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions
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

        Assertions.assertEquals(0, counter.get())
        debouncedRunnable.run()
        Assertions.assertEquals(0, counter.get())

        Assertions.assertTrue(latch.await(delay + 200, TimeUnit.MILLISECONDS))
        Assertions.assertEquals(1, counter.get())
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

        Assertions.assertTrue(latch.await(delay + 200, TimeUnit.MILLISECONDS))
        Assertions.assertEquals(1, counter.get())
    }

    @Test
    fun testExecuteNow() {
        val delay: Long = 500

        debouncedRunnable = DebouncedRunnable({ counter.incrementAndGet() }, delay, null)

        debouncedRunnable.run()
        Assertions.assertEquals(0, counter.get())

        debouncedRunnable.executeNow()
        Assertions.assertEquals(1, counter.get())
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
        Assertions.assertTrue(latch.await(delay + 200, TimeUnit.MILLISECONDS))

        Assertions.assertEquals(1, counter.get())
        Assertions.assertEquals(1, callbackCounter.get())
    }

    @Test
    fun testGetDelayMillis() {
        val delay: Long = 500
        debouncedRunnable = DebouncedRunnable({}, delay, null)

        Assertions.assertEquals(delay, debouncedRunnable.delayMillis)
    }
}
