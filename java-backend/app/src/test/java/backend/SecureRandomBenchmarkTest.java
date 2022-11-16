package backend;

import org.junit.jupiter.api.Test;
import java.security.SecureRandom;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * I must be able to make at least 1000 secure randoms per second
 *
 * @author Danny
 * @version 1
 */
public class SecureRandomBenchmarkTest {
    @Test void testRandomSpeed() {
        final SecureRandom rand = new SecureRandom();
        final long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            rand.nextLong();
        }

        final long endTime = System.currentTimeMillis();

        assertTrue(endTime - startTime <= 1000L);
    }
}
