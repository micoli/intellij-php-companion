package org.micoli.php;

import java.time.Duration;
import junit.framework.TestCase;
import org.micoli.php.service.ConcurrentSearchManager;

public class ConcurrentSearchManagerTest extends TestCase {

    public void testItAllowToAddASearchIfIsNotPresent() {
        ConcurrentSearchManager concurrentSearchManager = new ConcurrentSearchManager(Duration.ofSeconds(20));
        concurrentSearchManager.addSearch("test");
        assertTrue(concurrentSearchManager.isSearchInProgress("test"));
        assertFalse(concurrentSearchManager.isSearchInProgress("test2"));
        concurrentSearchManager.removeSearch("test");
        assertFalse(concurrentSearchManager.isSearchInProgress("test"));
    }

    public void testItDisallowToAddASearchIfIsPresent() throws InterruptedException {
        ConcurrentSearchManager concurrentSearchManager = new ConcurrentSearchManager(Duration.ofSeconds(2));
        concurrentSearchManager.addSearch("test");
        assertTrue(concurrentSearchManager.isSearchInProgress("test"));
        Thread.sleep(3000);
        assertFalse(concurrentSearchManager.isSearchInProgress("test"));
        assertTrue(concurrentSearchManager.isEmpty());
    }
}
