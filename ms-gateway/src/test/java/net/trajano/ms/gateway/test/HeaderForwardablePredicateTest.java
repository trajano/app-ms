package net.trajano.ms.gateway.test;

import static junit.framework.TestCase.assertTrue;
import static net.trajano.ms.gateway.providers.RequestIDProvider.REQUEST_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import net.trajano.ms.gateway.internal.Predicates;

public class HeaderForwardablePredicateTest {

    /**
     * Tests the predicate.
     */
    @Test
    public void testPredicate() {

        final Map<String, String> map = new LinkedHashMap<>();
        map.put("foo", "XXX");
        map.put(REQUEST_ID, "XXX");
        map.put("bar", "XXX");
        final Iterator<Map.Entry<String, String>> x = map.entrySet().iterator();

        assertTrue(Predicates.HEADER_FORWARDABLE.test(x.next()));
        assertFalse(Predicates.HEADER_FORWARDABLE.test(x.next()));
        assertTrue(Predicates.HEADER_FORWARDABLE.test(x.next()));
    }

    /**
     * Tests that bearer authorization is filtered.
     */
    @Test
    public void testRestrictedAuthorization() {

        final Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer abc");
        headers.put("X-JWT-Assertion", "asdf");
        headers.put("Date", "asdf");
        headers.put("Allowed", "asdf");
        final Map<String, String> y = headers.entrySet().stream().filter(Predicates.HEADER_FORWARDABLE).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        assertTrue(y.containsKey("Allowed"));
        assertEquals("Map should only have one entry " + y, 1, y.size());

    }

    @Test
    public void testStream() {

        final Map<String, String> map = new LinkedHashMap<>();
        map.put("foo", "XXX");
        map.put(REQUEST_ID, "XXX");
        map.put("bar", "XXX");
        final Map<String, String> y = map.entrySet().stream().filter(Predicates.HEADER_FORWARDABLE).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        assertTrue(y.containsKey("foo"));
        assertTrue(y.containsKey("bar"));
        assertFalse(y.containsKey(REQUEST_ID));
    }

    /**
     * Tests that basic authorization is not filtered.
     */
    @Test
    public void testUnrestrictedAuthorization() {

        final Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic abc");
        headers.put("X-JWT-Assertion", "asdf");
        headers.put("Date", "asdf");
        headers.put("Allowed", "asdf");
        final Map<String, String> y = headers.entrySet().stream().filter(Predicates.HEADER_FORWARDABLE).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        assertTrue(y.containsKey("Allowed"));
        assertTrue(y.containsKey("Authorization"));
        assertEquals("Map should only have two entries" + y, 2, y.size());

    }
}
