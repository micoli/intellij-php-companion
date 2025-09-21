package org.micoli.php

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.micoli.php.service.DebouncedRunnable
import org.micoli.php.service.DebouncedRunnables

class DebouncedRunnablesTest {
    private var debouncedRunnables: DebouncedRunnables? = null
    private var counter: AtomicInteger? = null

    @Before
    fun setUp() {
        debouncedRunnables = DebouncedRunnables()
        counter = AtomicInteger(0)
    }

    @After
    fun tearDown() {
        debouncedRunnables!!.reset()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRunWithName() {
        val delay: Long = 300
        val latch = CountDownLatch(1)

        val runnable =
          debouncedRunnables!!.run(
            {
                counter!!.incrementAndGet()
                latch.countDown()
            },
            "testRunnable",
            delay,
          )

        Assertions.assertNotNull(runnable)
        Assertions.assertEquals(0, counter!!.get())

        Assertions.assertTrue(latch.await(delay + 200, TimeUnit.MILLISECONDS))
        Assertions.assertEquals(1, counter!!.get())
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRunReusesSameRunnableForSameName() {
        val delay: Long = 600
        val latch = CountDownLatch(1)
        val callCounter = AtomicInteger(0)

        val runnable1 = getSameNameRunnable(callCounter, latch, delay)
        for (_i in 0..9) {
            getSameNameRunnable(callCounter, latch, delay)
        }
        Thread.sleep(100)
        val runnable2 = getSameNameRunnable(callCounter, latch, delay)

        Assertions.assertSame(runnable1, runnable2)

        Assertions.assertTrue(latch.await(delay + 200, TimeUnit.MILLISECONDS))
        Assertions.assertEquals(1, callCounter.get())
    }

    private fun getSameNameRunnable(callCounter: AtomicInteger, latch: CountDownLatch, delay: Long): DebouncedRunnable? =
      debouncedRunnables!!.run(
        {
            counter!!.incrementAndGet()
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

        debouncedRunnables!!.run(
          {
              counter!!.incrementAndGet()
              taskLatch.countDown()
          },
          "runnableWithCallback",
          delay,
          {
              callbackCounter.incrementAndGet()
              callbackLatch.countDown()
          },
        )

        Assertions.assertTrue(taskLatch.await(delay + 200, TimeUnit.MILLISECONDS))
        Assertions.assertTrue(callbackLatch.await(100, TimeUnit.MILLISECONDS))

        Assertions.assertEquals(1, counter!!.get())
        Assertions.assertEquals(1, callbackCounter.get())
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
          debouncedRunnables!!.run(
            {
                counter1.incrementAndGet()
                latch1.countDown()
            },
            "runnable1",
            delay,
          )

        val runnable2 =
          debouncedRunnables!!.run(
            {
                counter2.incrementAndGet()
                latch2.countDown()
            },
            "runnable2",
            delay,
          )

        Assertions.assertNotSame(runnable1, runnable2)

        Assertions.assertTrue(latch1.await(delay + 200, TimeUnit.MILLISECONDS))
        Assertions.assertTrue(latch2.await(100, TimeUnit.MILLISECONDS))

        Assertions.assertEquals(1, counter1.get())
        Assertions.assertEquals(1, counter2.get())
    }

    @Test
    @Throws(InterruptedException::class)
    fun testReset() {
        val delay: Long = 400

        debouncedRunnables!!.run({ counter!!.incrementAndGet() }, "runnable1", delay)
        debouncedRunnables!!.run({ counter!!.incrementAndGet() }, "runnable2", delay)

        debouncedRunnables!!.reset()

        Thread.sleep(delay + 1700)

        Assertions.assertEquals(0, counter!!.get())
    }
}
