package net.trajano.ms.authz.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

@Component
public class LoggingEntryListener implements
    EntryAddedListener<String, Object>,
    EntryRemovedListener<String, Object>,
    EntryUpdatedListener<String, Object>,
    EntryEvictedListener<String, Object> {

    /**
     * Reuse the same logger as {@link HazelcastConfiguration}.
     */
    private static final Logger LOG = LoggerFactory.getLogger(HazelcastConfiguration.class);

    @Override
    public void entryAdded(final EntryEvent<String, Object> event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("{} name={} key={}", event.getEventType(), event.getName(), event.getKey());
        }

    }

    @Override
    public void entryEvicted(final EntryEvent<String, Object> event) {

        entryAdded(event);
    }

    @Override
    public void entryRemoved(final EntryEvent<String, Object> event) {

        entryAdded(event);
    }

    @Override
    public void entryUpdated(final EntryEvent<String, Object> event) {

        entryAdded(event);
    }
}
