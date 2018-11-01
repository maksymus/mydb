package org.mydb.util;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.function.Supplier;

/**
 * Pool of objects.
 * TODO add pool max size to restrict num of objects
 */
class ObjectPool<T> {
    /** Object factory */
    private final Supplier<T> factory;

    /** Object position in pool*/
    private final IdentityHashMap<T, Integer> positionMap = new IdentityHashMap<>();

    /** Number of objects currently checked out */
    private int acquiredSize;

    /** Object pool */
    private Object[] pool;

    public ObjectPool(Supplier<T> factory) {
        // create pool with 10 objects by default
        this(factory, 10);
    }

    public ObjectPool(Supplier<T> factory, int initSize) {
        if (initSize <= 0)
            new IllegalArgumentException("pool size should be > 0");

        this.factory = factory;
        this.pool = new Object[initSize];

        for (int i = 0; i < initSize; i++) {
            T pooled = factory.get();
            pool[i] = pooled;
            positionMap.put(pooled, i);
        }
    }

    public synchronized T acquire() {
        if (acquiredSize >= pool.length) {
            Object[] newPool = Arrays.copyOf(pool, pool.length * 2);
            for (int i = pool.length; i < newPool.length; i++) {
                T pooled = factory.get();
                newPool[i] = pooled;
                positionMap.put(pooled, i);
            }

            pool = newPool;
        }

        return (T) pool[acquiredSize++];
    }

    public synchronized void release(T object) {
        Integer position = positionMap.get(object);
        // no such object in pool
        if (position == null)
            return;

        // object already released
        if (position >= acquiredSize)
            return;

        swap(position, acquiredSize - 1);

        if (acquiredSize > 0)
            acquiredSize--;
    }

    private void swap(int from, int to) {
        if (from == to)
            return;

        T fromObject = (T) pool[from];
        T toObject = (T) pool[to];

        pool[to] = fromObject;
        pool[from] = toObject;

        positionMap.replace(fromObject, to);
        positionMap.replace(toObject, from);
    }

    public static void main(String[] args) throws InterruptedException {
        ObjectPool<Object> pool = new ObjectPool<>(() -> {
            return new Object();
        }, 2);

        Object obj1 = pool.acquire();
        Object obj2 = pool.acquire();

        pool.release(obj1);
        pool.release(obj1);
    }
}
