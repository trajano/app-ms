package net.trajano.ms.engine.internal;

import java.io.IOException;
import java.io.InputStream;

public class NullInputStream extends InputStream {

    private static final NullInputStream INSTANCE = new NullInputStream();

    public static InputStream nullInputStream() {

        return INSTANCE;
    }

    @Override
    public int read() throws IOException {

        return -1;
    }

}
