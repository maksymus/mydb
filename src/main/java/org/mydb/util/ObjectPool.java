package org.mydb.util;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Pool of objects.
 */
class ObjectPool<T> {

    /** Default pool size */
    public static final int DEFAULT_POOL_SIZE = 10;

    /** Object factory */
    private final Supplier<T> factory;

    /** Object position in pool*/
    private final IdentityHashMap<T, Integer> positions = new IdentityHashMap<>();

    /** Number of objects currently checked out */
    private int acquiredSize;

    /** Object pool */
    private Object[] pool;

    /**
     * Create pool with default size.
     * @param factory object factory.
     */
    public ObjectPool(Supplier<T> factory) {
        this(factory, DEFAULT_POOL_SIZE);
    }

    public ObjectPool(Supplier<T> factory, int initSize) {
        if (initSize <= 0) {
            throw new IllegalArgumentException("pool size should be > 0");
        }

        this.factory = factory;
        this.pool = new Object[initSize];

        for (int i = 0; i < initSize; i++) {
            T pooled = factory.get();
            pool[i] = pooled;
            positions.put(pooled, i);
        }
    }

    public synchronized T acquire(Function<T, T> initiate) {
        if (acquiredSize >= pool.length) {
            Object[] newPool = Arrays.copyOf(pool, pool.length * 2);
            for (int i = pool.length; i < newPool.length; i++) {
                T pooled = factory.get();
                newPool[i] = pooled;
                positions.put(pooled, i);
            }

            pool = newPool;
        }

        return initiate.apply((T) pool[acquiredSize++]);
    }

    public synchronized T acquire() {
        return acquire(Function.identity());
    }

    public synchronized void release(T object, Function<T, T> clear) {
        Integer position = positions.get(object);
        // no such object in pool
        if (position == null)
            return;

        // object already released
        if (position >= acquiredSize)
            return;

        clear.apply(object);
        swap(position, acquiredSize - 1);

        if (acquiredSize > 0)
            acquiredSize--;
    }

    public synchronized void release(T object) {
        release(object, Function.identity());
    }

    private void swap(int from, int to) {
        if (from == to)
            return;

        T fromObject = (T) pool[from];
        T toObject = (T) pool[to];

        pool[to] = fromObject;
        pool[from] = toObject;

        positions.replace(fromObject, to);
        positions.replace(toObject, from);
    }
}
