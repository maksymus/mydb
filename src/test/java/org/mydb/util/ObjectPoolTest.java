package org.mydb.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;

public class ObjectPoolTest {

    @Test
    public void init_state() {
        ObjectPool<Object> pool = new ObjectPool<>(Object::new);

        Object[] internPool = getInternalState(pool, "pool");
        IdentityHashMap<Object, Integer> internPositions = getInternalState(pool, "positions");

        Assert.assertEquals(ObjectPool.DEFAULT_POOL_SIZE, internPool.length);
        Assert.assertEquals(ObjectPool.DEFAULT_POOL_SIZE, internPositions.size());

        Assert.assertThat(pool, new ObjectPoolInternalStateMatcher(Arrays.asList()));
    }

    @Test
    public void acquire_resize() {
        ObjectPool<Object> pool = new ObjectPool<>(Object::new, 1);

        Object obj1 = pool.acquire();
        Object obj2 = pool.acquire();
        Object obj3 = pool.acquire();

        Object[] internPool = getInternalState(pool, "pool");
        IdentityHashMap<Object, Integer> internPositions = getInternalState(pool, "positions");

        Assert.assertEquals(4, internPool.length);
        Assert.assertEquals(4, internPositions.size());

        Assert.assertThat(pool, new ObjectPoolInternalStateMatcher(Arrays.asList(obj1, obj2, obj3)));
    }

    @Test (expected = IllegalArgumentException.class)
    public void init_zeroSize() {
        new ObjectPool<>(Object::new, 0);
    }

    @Test
    public void release() {
        ObjectPool<Object> pool = new ObjectPool<>(Object::new, 1);

        Object obj1 = pool.acquire();
        Object obj2 = pool.acquire();
        Object obj3 = pool.acquire();
        Object obj4 = pool.acquire();

        pool.release(obj2);

        Assert.assertThat(pool, new ObjectPoolInternalStateMatcher(Arrays.asList(obj1, obj3, obj4)));
    }

    @Test
    public void release_all() {
        ObjectPool<Object> pool = new ObjectPool<>(Object::new, 1);

        Object obj1 = pool.acquire();
        Object obj2 = pool.acquire();
        Object obj3 = pool.acquire();
        Object obj4 = pool.acquire();

        pool.release(obj2);
        pool.release(obj1);
        pool.release(obj4);
        pool.release(obj3);

        Assert.assertThat(pool, new ObjectPoolInternalStateMatcher(Arrays.asList()));
    }

    @Test
    public void acquire_callback() {
        ObjectPool<TestObject> pool = new ObjectPool<>(TestObject::new);

        TestObject obj1 = pool.acquire((obj) -> obj.setProp("hello"));

        Assert.assertThat(obj1.getProp(), Matchers.is("hello"));
        Assert.assertThat(pool, new ObjectPoolInternalStateMatcher(Arrays.asList(obj1)));
    }

    @Test
    public void release_callback() {
        ObjectPool<TestObject> pool = new ObjectPool<>(TestObject::new);

        TestObject obj1 = pool.acquire((obj) -> obj.setProp("hello"));
        pool.release(obj1, (obj) -> obj.setProp("bye"));

        Assert.assertThat(obj1.getProp(), Matchers.is("bye"));
        Assert.assertThat(pool, new ObjectPoolInternalStateMatcher(Arrays.asList()));
    }

    private static <T, R> R getInternalState(ObjectPool<T> objectPool, String fileName) {
        return (R) Whitebox.getInternalState(objectPool, fileName);
    }

    /**
     * Internal state matcher
     */
    private static class ObjectPoolInternalStateMatcher<T> extends BaseMatcher<ObjectPool<T>> {

        private List<T> acquiredObjects;

        public ObjectPoolInternalStateMatcher(List<T> acquiredObjects) {
            this.acquiredObjects = acquiredObjects;
        }

        @Override
        public boolean matches(Object item) {
            ObjectPool<T> objectPool = (ObjectPool<T>) item;

            Object[] internPool = getInternalState(objectPool, "pool");
            IdentityHashMap<T, Integer> internPositions = getInternalState(objectPool, "positions");
            Integer acquiredSize = getInternalState(objectPool, "acquiredSize");

            for (int i = 0; i < internPool.length; i++) {
                if (i != internPositions.get(internPool[i]))
                    return false;
            }

            if (acquiredObjects.size() != acquiredSize)
                return false;

            for (T acquiredObject : acquiredObjects) {
                if (internPositions.get(acquiredObject) >= acquiredSize) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("object pool internal state not matching");
        }
    }

    /**
     * Test object with property
     */
    private static class TestObject {
        private String prop;

        public TestObject setProp(String prop) {
            this.prop = prop;
            return this;
        }

        public String getProp() {
            return prop;
        }
    }
}