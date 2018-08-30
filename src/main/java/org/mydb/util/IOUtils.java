package org.mydb.util;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {
    /**
     * Silent close io stream.
     * @param stream stream to close.
     */
    public static <T extends Closeable> void close(T stream) {
        if (stream == null)
            return;

        try {
            stream.close();
        } catch (IOException ignore) {}
    }
}
