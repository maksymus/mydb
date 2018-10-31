package org.mydb.util.cache;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

public class LRUCacheTest {
    @Test
    public void emptyGet() {
        LRUCache<Integer, String> cache = new LRUCache<>(10);
        Assert.assertThat(cache.get(1), Matchers.nullValue());
    }

    @Test
    public void put_get() {
        LRUCache<Integer, String> cache = new LRUCache<>(10);
        cache.put(1, "hello");

        Assert.assertThat(cache.get(1), Matchers.equalTo("hello"));
    }

    @Test
    public void cacheSize() {
        LRUCache<Integer, String> cache = new LRUCache<>(2);
        cache.put(1, "hello");
        cache.put(2, "hello");
        cache.put(3, "hello");

        Assert.assertThat(cache.getCache().size(), Matchers.equalTo(2));
    }

    @Test
    public void itemUpdated() {
        LRUCache<Integer, Character> cache = new LRUCache<>(10);
        cache.put(1, 'a');
        cache.put(2, 'b');
        cache.put(3, 'c');
        cache.put(3, 'd');

        Character character = cache.get(3);

        Assert.assertThat(character, Matchers.equalTo('d'));
        Assert.assertThat(cache.getCache().size(), Matchers.equalTo(3));
    }

    @Test
    public void oneItem() {
        LRUCache<Integer, Character> cache = new LRUCache<>(1);
        cache.put(1, 'a');
        cache.put(2, 'b');
        cache.put(3, 'c');
        cache.put(3, 'd');

        Character character = cache.get(3);

        Assert.assertThat(character, Matchers.equalTo('d'));
        Assert.assertThat(cache.getCache().size(), Matchers.equalTo(1));
    }

    @Test
    public void moveToHead() {
        LRUCache<Integer, Character> cache = new LRUCache<>(10);
        cache.put(1, 'a');
        cache.put(2, 'b');
        cache.put(3, 'c');
        cache.put(4, 'd');

        Character character = cache.get(3);

        Assert.assertThat(character, Matchers.equalTo('c'));
        Assert.assertThat(cache.getCache().size(), Matchers.equalTo(4));
        Assert.assertThat(cache.getCache().keySet(), Matchers.hasItems(1, 2, 3, 4));
        Assert.assertThat(cache.getHead(), Matchers.is(new CacheNodeMatcher<>(3, 'c')));
        Assert.assertThat(cache.getTail(), Matchers.is(new CacheNodeMatcher<>(1, 'a')));
    }

    private static class CacheNodeMatcher<K, V> extends BaseMatcher<LRUCache.CacheNode<Integer, Character>> {
        private final K key;
        private final V value;

        public CacheNodeMatcher(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean matches(Object obj) {
            LRUCache.CacheNode<Integer, Character> node = (LRUCache.CacheNode<Integer, Character>) obj;
            return Objects.equals(key, node.key) && Objects.equals(value, node.value);
        }

        @Override
        public void describeTo(Description description) {}
    }
}