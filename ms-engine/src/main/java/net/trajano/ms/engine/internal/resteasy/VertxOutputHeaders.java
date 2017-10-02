package net.trajano.ms.engine.internal.resteasy;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.AbstractMultivaluedMap;
import javax.ws.rs.core.MultivaluedMap;

import io.vertx.core.http.HttpServerResponse;

public class VertxOutputHeaders extends AbstractMultivaluedMap<String, String> implements
    MultivaluedMap<String, String> {

    private static class VertxOutputHeadersMap extends AbstractMap<String, List<String>> {

        private final HttpServerResponse vertxResponse;

        public VertxOutputHeadersMap(final HttpServerResponse vertxResponse) {

            this.vertxResponse = vertxResponse;

        }

        @Override
        public Set<Entry<String, List<String>>> entrySet() {

            final Map<String, List<String>> ret = new HashMap<>();
            vertxResponse.headers().forEach(entry -> {
                final List<String> list = ret.getOrDefault(entry.getKey(), new LinkedList<>());
                ret.put(entry.getKey(), list);
            });
            return ret.entrySet();
        }

        @Override
        public List<String> put(final String key,
            final List<String> values) {

            final List<String> prev = vertxResponse.headers().getAll(key);
            vertxResponse.putHeader(key, values);
            return prev;
        }
    }

    public VertxOutputHeaders(final HttpServerResponse vertxResponse) {

        super(new VertxOutputHeadersMap(vertxResponse));
    }
}
