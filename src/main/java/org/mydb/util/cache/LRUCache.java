package org.mydb.util.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * LRU cache implementation
 * @param <K> key
 * @param <V> value
 */
public class LRUCache<K, V> implements Cache<K,V> {

    /** Cache content */
    private final Map<K, CacheNode<K, V>> cache = new HashMap<>();

    /** Linked list to track least recently used - first item is least recently used*/
    private CacheNode<K, V> head;
    private CacheNode<K, V> tail;

    private final int maxNumItems;

    public LRUCache(int numItems) {
        if (numItems < 1)
            throw new IllegalArgumentException("cache size should be > 0");

        this.maxNumItems = numItems;
    }

    public synchronized V put(K key, V value) {
        // TODO keep CacheNode pool to avoid new CacheNode() - GC optimization
        CacheNode<K, V> cacheNode = cache.containsKey(key) ? cache.get(key) : new CacheNode<>();

        cacheNode.key = key;
        cacheNode.value = value;

        cache.put(key, cacheNode);

        moveToHead(cacheNode);
        removeTail();

        return cacheNode.value;
    }

    public synchronized V get(K key) {
        if (cache.containsKey(key)) {
            CacheNode<K, V> cacheNode = cache.get(key);
            moveToHead(cacheNode);
            return cacheNode.value;
        }

        return null;
    }

    // internal ==============================================================================
    private void removeTail() {
        if (tail == null)
            return;

        if (cache.size() <= maxNumItems)
            return;

        K key = tail.key;
        cache.remove(key);

        CacheNode<K, V> prev = tail.prev;

        tail.next = null;
        tail.prev = null;
        tail.key = null;
        tail.value = null;

        if (prev != null)
            prev.next = null;

        tail = prev;
    }

    private void moveToHead(CacheNode<K, V> node) {
        if (head == node)
            return;

        CacheNode<K, V> prev = node.prev;
        CacheNode<K, V> next = node.next;

        if (prev != null)
            prev.next = next;

        if (next != null)
            next.prev = prev;

        node.prev = null;
        node.next = head;

        if (head != null)
            head.prev = node;

        if (tail == null)
            tail = node;

        head = node;
    }

    Map<K, CacheNode<K, V>> getCache() {
        return cache;
    }

    CacheNode<K, V> getHead() {
        return head;
    }

    CacheNode<K, V> getTail() {
        return tail;
    }

    /** Doubly linked list */
    static class CacheNode <K, V> {
        CacheNode<K, V> prev;
        CacheNode<K, V> next;
        K key;
        V value;
    }

    public static void main(String[] args) {
        LRUCache<Integer, Character> cache = new LRUCache<>(10);
        cache.put(1, 'a');
        cache.put(2, 'b');
        cache.put(3, 'c');

        cache.put(3, 'd');

        System.out.println(cache.get(3));
    }
}
