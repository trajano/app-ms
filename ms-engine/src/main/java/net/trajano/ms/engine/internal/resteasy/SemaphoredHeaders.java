package net.trajano.ms.engine.internal.resteasy;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.jboss.resteasy.core.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will prevent "gets" until a the lock is released.
 *
 * @author Archimedes Trajano
 * @param <V>
 */
public class SemaphoredHeaders<V> extends Headers<V> {

    private static final Logger LOG = LoggerFactory.getLogger(SemaphoredHeaders.class);

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 6726444464471886920L;

    private final Semaphore metadataLock = new Semaphore(0);

    public void acquireUninterruptibly() {

        metadataLock.acquireUninterruptibly();
    }

    @Override
    public Set<Entry<String, List<V>>> entrySet() {

        LOG.debug("entrySet, available permits on lock={}", metadataLock.availablePermits());
        metadataLock.acquireUninterruptibly();
        try {
            return super.entrySet();
        } finally {
            metadataLock.release();
        }

    }

    @Override
    public boolean equals(final Object o) {

        LOG.debug("equals, available permits on lock={}", metadataLock.availablePermits());
        metadataLock.acquireUninterruptibly();
        try {
            return super.equals(o);
        } finally {
            metadataLock.release();
        }
    }

    @Override
    public List<V> get(final Object key) {

        LOG.debug("attempting to get {}, available permits on lock={}", key, metadataLock.availablePermits());
        metadataLock.acquireUninterruptibly();
        try {
            return super.get(key);
        } finally {
            metadataLock.release();
        }

    }

    @Override
    public int hashCode() {

        LOG.debug("hashCode, available permits on lock={}", metadataLock.availablePermits());
        metadataLock.acquireUninterruptibly();
        try {
            return super.hashCode();
        } finally {
            metadataLock.release();
        }

    }

    public void releaseLock() {

        metadataLock.release();
    }
}
