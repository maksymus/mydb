package org.mydb.util;

import org.junit.Test;

public class ObjectPoolTest {

    @Test (expected = Exception.class)
    public void acquire_zeroSize() {
        ObjectPool<Object> pool = new ObjectPool<>(Object::new, 0);
    }

    @Test
    public void release() {
    }
}