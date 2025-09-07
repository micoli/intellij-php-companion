package org.micoli.php;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.micoli.php.service.DebouncedRunnable;

public class DebouncedRunnableTest {
    private DebouncedRunnable debouncedRunnable;
    private AtomicInteger counter;
    private AtomicInteger callbackCounter;

    @Before
    public void setUp() {
        counter = new AtomicInteger(0);
        callbackCounter = new AtomicInteger(0);
    }

    @After
    public void tearDown() {
        if (debouncedRunnable != null) {
            debouncedRunnable.close();
        }
    }

    @Test
    public void testRunDelaysExecution() throws InterruptedException {
        long delay = 300;
        CountDownLatch latch = new CountDownLatch(1);

        debouncedRunnable = new DebouncedRunnable(
                () -> {
                    counter.incrementAndGet();
                    latch.countDown();
                },
                delay,
                null);

        assertEquals(0, counter.get());
        debouncedRunnable.run();
        assertEquals(0, counter.get());

        assertTrue(latch.await(delay + 200, TimeUnit.MILLISECONDS));
        assertEquals(1, counter.get());
    }

    @Test
    public void testMultipleRunCallsDebounce() throws InterruptedException {
        long delay = 300;
        CountDownLatch latch = new CountDownLatch(1);

        debouncedRunnable = new DebouncedRunnable(
                () -> {
                    counter.incrementAndGet();
                    latch.countDown();
                },
                delay,
                null);

        debouncedRunnable.run();
        Thread.sleep(100);
        debouncedRunnable.run();
        Thread.sleep(100);
        debouncedRunnable.run();

        assertTrue(latch.await(delay + 200, TimeUnit.MILLISECONDS));
        assertEquals(1, counter.get());
    }

    @Test
    public void testExecuteNow() {
        long delay = 500;

        debouncedRunnable = new DebouncedRunnable(counter::incrementAndGet, delay, null);

        debouncedRunnable.run();
        assertEquals(0, counter.get());

        debouncedRunnable.executeNow();
        assertEquals(1, counter.get());
    }

    @Test
    public void testCallbackIsExecuted() throws InterruptedException {
        long delay = 300;
        CountDownLatch latch = new CountDownLatch(1);

        debouncedRunnable = new DebouncedRunnable(counter::incrementAndGet, delay, () -> {
            callbackCounter.incrementAndGet();
            latch.countDown();
        });

        debouncedRunnable.run();
        assertTrue(latch.await(delay + 200, TimeUnit.MILLISECONDS));

        assertEquals(1, counter.get());
        assertEquals(1, callbackCounter.get());
    }

    @Test
    public void testGetDelayMillis() {
        long delay = 500;
        debouncedRunnable = new DebouncedRunnable(() -> {}, delay, null);

        assertEquals(delay, debouncedRunnable.getDelayMillis());
    }
}
