package backend;

import backend.authentication.NonceManager;
import backend.exceptions.NonceIssueException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NonceManagerTest {
    private static NonceManager manager;

    @BeforeAll static void testConstructor() {
        manager = new NonceManager();
    }

    @Test void testIssueNonce() throws NonceIssueException {
        final long nonceOne = manager.issueNonce();
        assertTrue(manager.isNonceValid(nonceOne));

        final long nonceTwo = manager.issueNonce();
        assertTrue(manager.isNonceValid(nonceTwo));

        assertNotEquals(nonceOne, nonceTwo);
    }

    @Test void testPerformance() throws NonceIssueException {
        final long start = System.currentTimeMillis();

        for (int i = 0; i < 1000; i ++) {
            manager.issueNonce();
        }

        final long end = System.currentTimeMillis();

        assertTrue(end - start <= 1000L);
    }

    @Test void testNonceIssueException() throws NonceIssueException {
        NonceManager manager = new NonceManager();

        for (int i = 0; i < NonceManager.MAX_NONCES; i ++) {
            assertTrue(manager.isNonceValid(manager.issueNonce()));
        }

        assertThrows(NonceIssueException.class, () -> {
            manager.issueNonce();
        });
    }

    @Test void testNonceExpire() throws NonceIssueException, InterruptedException {
        final long EXPIRE = 500L;
        NonceManager manager = new NonceManager(EXPIRE);
        List<Long> nonces = new LinkedList<>();

        // Add nonces
        for (int i = 0; i < NonceManager.MAX_NONCES; i ++) {
            nonces.add(manager.issueNonce());
        }

        assertThrows(NonceIssueException.class, () -> {
            manager.issueNonce();
        });

        // Test remove
        Thread.sleep(EXPIRE + 10L);
        manager.removeExpiredNonces();

        for (long nonce : nonces) {
            assertFalse(manager.isNonceValid(nonce));
        }

        // Test capacity
        for (int i = 0; i < NonceManager.MAX_NONCES; i ++) {
            assertTrue(manager.isNonceValid(manager.issueNonce()));
        }

        assertThrows(NonceIssueException.class, () -> {
            manager.issueNonce();
        });
    }

    @Test void testSingleUsage() throws NonceIssueException {
        final long nonceOne = manager.issueNonce();
        assertTrue(manager.isNonceValid(nonceOne));
        assertFalse(manager.isNonceValid(nonceOne));
    }

    @Test void testRemoveNoncesOnEmptyQueu() {
        NonceManager mangaer = new NonceManager();
        manager.removeExpiredNonces();
    }
}
