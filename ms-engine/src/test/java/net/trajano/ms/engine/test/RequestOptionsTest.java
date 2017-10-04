package net.trajano.ms.engine.test;

import java.net.URI;

import org.junit.Test;

import io.swagger.util.Json;
import io.vertx.core.http.RequestOptions;
import net.trajano.ms.engine.internal.Conversions;

public class RequestOptionsTest {

    @Test
    public void testConversions() {

        final URI uri = URI.create("https://github.com/eclipse/vert.x/blob/a30703379a06502faaae0aeda1eaa9a9b1152004/src/main/java/io/vertx/core/net/impl/SSLHelper.java");
        final RequestOptions requestOptions = Conversions.toRequestOptions(uri);
        Json.prettyPrint(requestOptions);

    }

    @Test
    public void testConversionsWithEscapedDataQuery() {

        final URI uri = URI.create("https://encrypted.google.com/search?q=face+book+%25+prime+%3F&oq=face+book+%25+prime+%3F&gs_l=psy-ab.3..0i13k1l10.12446.12733.0.13108.2.2.0.0.0.0.140.248.0j2.2.0....0...1.1.64.psy-ab..0.2.246....0.QRedmYd5GAY");
        final RequestOptions requestOptions = Conversions.toRequestOptions(uri);
        Json.prettyPrint(requestOptions);

    }

    @Test
    public void testConversionsWithQuery() {

        final URI uri = URI.create("https://news.google.com/news/?ned=ca&hl=en-CA");
        final RequestOptions requestOptions = Conversions.toRequestOptions(uri);
        Json.prettyPrint(requestOptions);

    }
}
