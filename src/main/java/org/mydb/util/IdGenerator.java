package org.mydb.util;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Unique id generator.
 */
public final class IdGenerator {
    public enum Type {
        CONNECTION,
        STATEMENT,
        XID
    }

    private static final AtomicLongArray generator = new AtomicLongArray(Type.values().length);

    private IdGenerator() {}

    public static long generate(Type type) {
        return generator.getAndIncrement(type.ordinal());
    }
}
