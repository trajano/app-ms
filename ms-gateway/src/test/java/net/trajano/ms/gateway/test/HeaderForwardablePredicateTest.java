package net.trajano.ms.gateway.test;

import static junit.framework.TestCase.assertTrue;
import static net.trajano.ms.gateway.providers.RequestIDProvider.REQUEST_ID;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import net.trajano.ms.gateway.internal.Predicates;

public class HeaderForwardablePredicateTest {

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

    @Test
    public void testStream() {

        final Map<String, String> map = new LinkedHashMap<>();
        map.put("foo", "XXX");
        map.put(REQUEST_ID, "XXX");
        map.put("bar", "XXX");
        final Map<String, String> y = map.entrySet().stream().filter(Predicates.HEADER_FORWARDABLE).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        assertTrue(y.containsKey("foo"));
        assertTrue(y.containsKey("foo"));
        assertFalse(y.containsKey(REQUEST_ID));
    }
}
