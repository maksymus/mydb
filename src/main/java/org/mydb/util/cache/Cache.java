package org.mydb.util.cache;

import java.util.function.Supplier;

/**
 * Cache interface
 * @param <K> key
 * @param <V> value
 */
public interface Cache<K, V> {
    V put(K key, V value);
    V get(K key);

    default V putIfAbsent(K key, Supplier<V> supplier)  {
        V cached = get(key);

        if (cached == null) {
            cached = put(key, supplier.get());
        }

        return cached;
    }
}
