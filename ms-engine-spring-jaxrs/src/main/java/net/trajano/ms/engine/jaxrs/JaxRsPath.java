package net.trajano.ms.engine.jaxrs;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class JaxRsPath implements
    Comparable<JaxRsPath> {

    /**
     * Content types that get consumed by the path.
     */
    private final String[] consumes;

    private final boolean exact;

    /**
     * VertX HTTP method.
     */
    private final HttpMethod method;

    /**
     * Path.
     */
    private final String path;

    /**
     * Path regular expression applies when {@link #exact} is false.
     */
    private final String pathRegex;

    /**
     * Content types that are produced by the path.
     */
    private final String[] produces;

    /**
     * Constructs JaxRsPath.
     *
     * @param path
     *            path
     * @param consumes
     *            content types consumed
     * @param produces
     *            content types produced
     * @param method
     *            HTTP method.
     */
    public JaxRsPath(final String path,
        final String[] consumes,
        final String[] produces,
        final HttpMethod method) {

        if (path.isEmpty()) {
            throw new IllegalArgumentException("path cannot be empty");
        }
        this.path = path;
        this.consumes = consumes;
        this.produces = produces;
        this.method = method;
        final Pattern placeholderPattern = Pattern.compile("/\\{([^}]+)}");
        final Pattern regexPlaceholderPattern = Pattern.compile("[-A-Za-z_0-9]+:\\s*(.+)");
        final Matcher matcher = placeholderPattern.matcher(path);

        final StringBuffer b = new StringBuffer();
        while (matcher.find()) {
            final Matcher m2 = regexPlaceholderPattern.matcher(matcher.group(1));
            if (m2.matches()) {
                matcher.appendReplacement(b, "/" + m2.group(1));
            } else {
                matcher.appendReplacement(b, "/[^/]+");
            }
        }
        matcher.appendTail(b);
        pathRegex = b.toString();
        exact = pathRegex.equals(path);
    }

    /**
     * Apply path to router and assign the appropriate handlers.
     *
     * @param router
     *            router
     * @param jaxRsHandler
     *            JAX-RS Handler
     * @param failureHandler
     *            failure handler
     */
    public void apply(final Router router,
        final Handler<RoutingContext> jaxRsHandler,
        final Handler<RoutingContext> failureHandler) {

        if (isGet()) {
            if (isExact()) {
                router.head(getPath());
            } else {
                router.headWithRegex(getPathRegex());
            }
        }

        Route route;
        if (isExact()) {
            route = router.route(getMethod(), getPath());
        } else {
            route = router.routeWithRegex(getMethod(), getPathRegex());
        }

        for (final String mimeType : consumes) {
            route = route.consumes(mimeType);
        }
        for (final String mimeType : produces) {
            route = route.produces(mimeType);
        }
        route.handler(jaxRsHandler).failureHandler(failureHandler);
    }

    /**
     * Compare two arrays with the following rules. The longer the length, the
     * higher it's sequence. If equal length them each entry is compared.
     */
    private <T extends Comparable<T>> int compareArrays(final T[] a1,
        final T[] a2) {

        if (a1.length != a2.length) {
            return a1.length - a2.length;
        }

        for (int i = 0; i < a1.length; ++i) {
            if (!a1[i].equals(a2[i])) {
                return a1[i].compareTo(a2[i]);
            }
        }

        return 0;

    }

    /**
     * Compares two JaxRsPath objects such that it is ordered by most specific and
     * lowest level first. It sorts it in reverse by path. A path with produces is
     * order before one than one that does not. {@inheritDoc}
     */
    @Override
    public int compareTo(final JaxRsPath o) {

        if (exact && !o.exact) {
            return -1;
        } else if (!exact && o.exact) {
            return 1;
        }

        final int c = o.path.compareTo(path);
        if (c != 0) {
            return c;
        }

        final int producesComparison = compareArrays(produces, o.produces);
        if (producesComparison != 0) {
            return producesComparison;
        }

        final int consumesComparison = compareArrays(consumes, o.consumes);
        if (consumesComparison != 0) {
            return consumesComparison;
        }

        return method.compareTo(o.method);

    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JaxRsPath other = (JaxRsPath) obj;
        if (!Arrays.equals(consumes, other.consumes)) {
            return false;
        }
        if (method != other.method) {
            return false;
        }
        if (!Objects.equals(path, other.path)) {
            return false;
        }
        return Arrays.equals(produces, other.produces);
    }

    /**
     * Gets the VertX HTTP method.
     *
     * @return VertX HTTP method
     */
    public HttpMethod getMethod() {

        return method;
    }

    /**
     * Gets the path that is specified in the JAX-RS classes.
     *
     * @return path
     */
    public String getPath() {

        return path;
    }

    /**
     * Gets the path that is suitable for the router.
     *
     * @return path regular expression
     */
    public String getPathRegex() {

        return pathRegex;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(consumes);
        result = prime * result + (method == null ? 0 : method.hashCode());
        result = prime * result + (path == null ? 0 : path.hashCode());
        result = prime * result + Arrays.hashCode(produces);
        return result;
    }

    /**
     * Checks if the path is an exact one (i.e. no regex)
     *
     * @return exact path indicator
     */
    public boolean isExact() {

        return exact;
    }

    private boolean isGet() {

        return method == HttpMethod.GET;
    }

    @Override
    public String toString() {

        return "JaxRsPath [consumes=" + Arrays.toString(consumes) + ", exact=" + exact + ", method=" + method + ", path=" + path + ", pathRegex=" + pathRegex + ", produces=" + Arrays.toString(produces) + "]";
    }

}
