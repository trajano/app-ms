package net.trajano.ms.engine.jaxrs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JaxRsPath implements
    Comparable<JaxRsPath> {

    /**
     * Mime types that get consumed by the path. This is expected to be empty for
     * GET requests.
     */
    private final String[] consumes;

    private final boolean delete;

    private final boolean exact;

    /**
     * Flag for GET and HEAD requests.
     */
    private final boolean get;

    private final String path;

    private final String pathRegex;

    private final boolean post;

    private final String[] produces;

    private final boolean put;

    public JaxRsPath(final String path,
        final String[] consumes,
        final String[] produces,
        final boolean get,
        final boolean post,
        final boolean put,
        final boolean delete) {

        this.path = path;
        this.consumes = consumes;
        this.produces = produces;
        this.get = get;
        this.post = post;
        this.put = put;
        this.delete = delete;
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
     * Compares two JaxRsPath objects such that it is ordered by most specific and
     * lowest level first. It sorts it in reverse by path. {@inheritDoc}
     */
    @Override
    public int compareTo(final JaxRsPath o) {

        if (exact && !o.exact) {
            return -1;
        } else if (!exact && o.exact) {
            return 1;
        }

        return o.path.compareTo(path);
    }

    public String[] getConsumes() {

        return consumes;
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

    public String[] getProduces() {

        return produces;
    }

    public boolean isDelete() {

        return delete;
    }

    /**
     * Checks if the path is an exact one (i.e. no regex)
     *
     * @return exact path indicator
     */
    public boolean isExact() {

        return exact;
    }

    public boolean isGet() {

        return get;
    }

    public boolean isPost() {

        return post;
    }

    public boolean isPut() {

        return put;
    }
}
