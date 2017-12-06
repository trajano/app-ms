package net.trajano.ms.core;

/**
 * Operations on nonces. In cryptography, a nonce is an arbitrary number that
 * can only be used once. It is similar in spirit to a nonce word, hence the
 * name. It is often a random or pseudo-random number issued in an
 * authentication protocol to ensure that old communications cannot be reused in
 * replay attacks. They can also be useful as initialization vectors and in
 * cryptographic hash functions.
 */
public interface NonceOps {

    /**
     * Claims the nonce so it cannot be used anymore.
     *
     * @param nonce
     *            nonce token
     * @return true if the nonce is valid and claimed successfully.
     */
    boolean claimNonce(String nonce);

    /**
     * Generates a nonce token suitable for single use operations to prevent double
     * submission or to prevent CSRF.
     *
     * @return string token
     */
    String newNonce();
}
