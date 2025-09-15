package org.micoli.php;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.micoli.php.service.DebouncedRunnable;
import org.micoli.php.service.DebouncedRunnables;

public class DebouncedRunnablesTest {
    private DebouncedRunnables debouncedRunnables;
    private AtomicInteger counter;

    @Before
    public void setUp() {
        debouncedRunnables = new DebouncedRunnables();
        counter = new AtomicInteger(0);
    }

    @After
    public void tearDown() {
        debouncedRunnables.reset();
    }

    @Test
    public void testRunWithName() throws InterruptedException {
        long delay = 300;
        CountDownLatch latch = new CountDownLatch(1);

        DebouncedRunnable runnable = debouncedRunnables.run(
                () -> {
                    counter.incrementAndGet();
                    latch.countDown();
                },
                "testRunnable",
                delay);

        assertNotNull(runnable);
        assertEquals(0, counter.get());

        assertTrue(latch.await(delay + 200, TimeUnit.MILLISECONDS));
        assertEquals(1, counter.get());
    }

    @Test
    public void testRunReusesSameRunnableForSameName() throws InterruptedException {
        long delay = 600;
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger callCounter = new AtomicInteger(0);

        DebouncedRunnable runnable1 = getSameNameRunnable(callCounter, latch, delay);
        for (int i = 0; i < 10; i++) {
            getSameNameRunnable(callCounter, latch, delay);
        }
        Thread.sleep(100);
        DebouncedRunnable runnable2 = getSameNameRunnable(callCounter, latch, delay);

        assertSame(runnable1, runnable2);

        assertTrue(latch.await(delay + 200, TimeUnit.MILLISECONDS));
        assertEquals(1, callCounter.get());
    }

    private DebouncedRunnable getSameNameRunnable(AtomicInteger callCounter, CountDownLatch latch, long delay) {
        return debouncedRunnables.run(
                () -> {
                    counter.incrementAndGet();
                    callCounter.incrementAndGet();
                    latch.countDown();
                },
                "sameNameRunnable",
                delay);
    }

    @Test
    public void testRunWithCallback() throws InterruptedException {
        long delay = 300;
        CountDownLatch taskLatch = new CountDownLatch(1);
        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicInteger callbackCounter = new AtomicInteger(0);

        debouncedRunnables.run(
                () -> {
                    counter.incrementAndGet();
                    taskLatch.countDown();
                },
                "runnableWithCallback",
                delay,
                () -> {
                    callbackCounter.incrementAndGet();
                    callbackLatch.countDown();
                });

        assertTrue(taskLatch.await(delay + 200, TimeUnit.MILLISECONDS));
        assertTrue(callbackLatch.await(100, TimeUnit.MILLISECONDS));

        assertEquals(1, counter.get());
        assertEquals(1, callbackCounter.get());
    }

    @Test
    public void testMultipleDistinctRunnables() throws InterruptedException {
        long delay = 300;
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);

        DebouncedRunnable runnable1 = debouncedRunnables.run(
                () -> {
                    counter1.incrementAndGet();
                    latch1.countDown();
                },
                "runnable1",
                delay);

        DebouncedRunnable runnable2 = debouncedRunnables.run(
                () -> {
                    counter2.incrementAndGet();
                    latch2.countDown();
                },
                "runnable2",
                delay);

        assertNotSame(runnable1, runnable2);

        assertTrue(latch1.await(delay + 200, TimeUnit.MILLISECONDS));
        assertTrue(latch2.await(100, TimeUnit.MILLISECONDS));

        assertEquals(1, counter1.get());
        assertEquals(1, counter2.get());
    }

    @Test
    public void testReset() throws InterruptedException {
        long delay = 400;

        debouncedRunnables.run(() -> counter.incrementAndGet(), "runnable1", delay);
        debouncedRunnables.run(() -> counter.incrementAndGet(), "runnable2", delay);

        debouncedRunnables.reset();

        Thread.sleep(delay + 1700);

        assertEquals(0, counter.get());
    }
}
