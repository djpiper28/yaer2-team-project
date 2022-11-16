package backend.authentication;

import backend.exceptions.NonceIssueException;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Manages all the nonces and when they expire and whether they have been used.
 *
 * @author Danny
 * @version 1
 */
public class NonceManager {
    /**
     * The maximum amount of nonces to issue
     *
     * @since 1
     */
    public static final int MAX_NONCES = 10000;
    /**
     * The maximum age of a nonce.
     *
     * @since 1
     */
    private static final long NONCE_EXPIRE = 1000L * 60L;
    /**
     * If a nonce has this as the expiry time then it has been used.
     *
     * @since 1
     */
    private static final long NONCE_USED = -1L;
    private final long nonceEXpire;
    /**
     * Stores a cache of nonces to the timestamp they were made that has O(1) access time.
     * A value of the nodes is the time it was issued.
     *
     * @since 1
     */
    private final Map<Long, Long> nonceMap;

    /**
     * A queue of the nonces, the first one is polled for removal.
     *
     * @since 1
     */
    private final Queue<Long> nonceQueue;

    /**
     * A securely random number genereator.
     *
     * @since 1
     */
    private final SecureRandom random;

    /**
     * Inits a nonoce manager.
     *
     * @since 1
     */
    public NonceManager() {
        this.nonceQueue = new LinkedList<>();
        this.nonceMap = new HashMap<>();
        this.random = new SecureRandom();
        this.nonceEXpire = NONCE_EXPIRE;
    }

    /**
     * <b>FOR TESTS ONLY</b>
     *
     * @param expirationTime how long it takes for a nonce to expire
     * @since 1
     */
    public NonceManager(long expirationTime) {
        this.nonceQueue = new LinkedList<>();
        this.nonceMap = new HashMap<>();
        this.random = new SecureRandom();
        this.nonceEXpire = expirationTime;
    }

    /**
     * Determine whether a nonce can be issued
     *
     * @return whether a nonce can be issued to a user
     * @since 1
     */
    private synchronized boolean canAddNonce() {
        return this.nonceQueue.size() < MAX_NONCES;
    }

    /**
     * Tries to issue a nonce for logging into an account, this is to check the liveliness and freshness of the
     * attempt to login/
     *
     * @return a valid nonce
     * @throws NonceIssueException thrown when no more nonces can be thrown
     */
    public synchronized long issueNonce() throws NonceIssueException {
        if (!this.canAddNonce()) {
            throw new NonceIssueException();
        }

        boolean flag = true;
        long time = NONCE_EXPIRE, nonce = 0L;

        while (flag) {
            time = System.currentTimeMillis();
            nonce = this.random.nextLong();

            flag = this.nonceMap.containsKey(nonce);
        }

        this.nonceQueue.add(nonce);
        this.nonceMap.put(nonce, time);

        return nonce;
    }

    /**
     * Returns whether a nonce is valid, if it is the system then marks it as used.
     *
     * @param nonce the nonce to check
     * @return whether the nonce was valid and can be used
     * @since 1
     */
    public synchronized boolean isNonceValid(long nonce) {
        // This nonce is not live
        if (!this.nonceMap.containsKey(nonce)) {
            return false;
        }

        // Check that the nonce has not been used
        final long issuedAt = this.nonceMap.get(nonce);
        if (issuedAt == NONCE_USED) {
            return false;
        }

        // Mark the nonce as used then return true
        this.nonceMap.replace(nonce, NONCE_USED);
        return true;
    }

    /**
     * This will remove all of the expired nonces from the nonce manager,
     *
     * @since 1
     */
    public synchronized void removeExpiredNonces() {
        boolean flag = this.nonceQueue.size() > 0;
        while (flag) {
            long nonce = this.nonceQueue.peek();

            if (System.currentTimeMillis() - this.nonceMap.get(nonce) >= this.nonceEXpire) {
                this.nonceMap.remove(nonce);
                this.nonceQueue.remove();

                flag = this.nonceQueue.size() > 0;
            } else {
                // Stop when there are no nonces to remove
                flag = false;
            }
        }
    }

}
