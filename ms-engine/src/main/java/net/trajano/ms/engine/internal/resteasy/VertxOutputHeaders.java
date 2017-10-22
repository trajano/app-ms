package net.trajano.ms.engine.internal.resteasy;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.AbstractMultivaluedMap;

import io.vertx.core.http.HttpServerResponse;

public class VertxOutputHeaders extends AbstractMultivaluedMap<String, Object> {

    private static class VertxOutputHeadersMap extends AbstractMap<String, List<Object>> {

        private final HttpServerResponse vertxResponse;

        public VertxOutputHeadersMap(final HttpServerResponse vertxResponse) {

            this.vertxResponse = vertxResponse;

        }

        @Override
        public Set<Entry<String, List<Object>>> entrySet() {

            final Map<String, List<Object>> ret = new HashMap<>();
            vertxResponse.headers().forEach(entry -> {
                final List<Object> list = ret.getOrDefault(entry.getKey(), new LinkedList<>());
                ret.put(entry.getKey(), list);
            });
            return ret.entrySet();
        }

        @Override
        public boolean equals(final Object o) {

            return super.equals(o);
        }

        @Override
        public int hashCode() {

            return super.hashCode();
        }

        @Override
        public List<Object> put(final String key,
            final List<Object> values) {

            final List<Object> prev = vertxResponse.headers().getAll(key).stream().map(v -> v).collect(Collectors.toList());
            final List<String> collect = values.stream().map(String::valueOf).collect(Collectors.toList());
            vertxResponse.putHeader(key, collect);
            return prev;
        }
    }

    public VertxOutputHeaders(final HttpServerResponse vertxResponse) {

        super(new VertxOutputHeadersMap(vertxResponse));
    }
}
