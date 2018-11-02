package org.mydb.util.cache;

import org.mydb.util.ObjectPool;

import java.util.HashMap;
import java.util.Map;

/**
 * LRU cache implementation
 * @param <K> key
 * @param <V> value
 */
public class LRUCache<K, V> implements Cache<K,V> {

    /** Cache size */
    private final int maxNumItems;

    /** Cache content */
    private final Map<K, CacheNode<K, V>> cache;

    /** Cache node pool to reduce GC overhead */
    private final ObjectPool<CacheNode<K, V>> cacheNodePool;

    /** Linked list to track least recently used - first item is least recently used*/
    private CacheNode<K, V> head;

    /** Tail of linked list starting with <code>head</code> */
    private CacheNode<K, V> tail;

    public LRUCache(int numItems) {
        if (numItems < 1)
            throw new IllegalArgumentException("cache size should be > 0");

        this.maxNumItems = numItems;
        this.cache = new HashMap<>();
        this.cacheNodePool = new ObjectPool<>(CacheNode::new);
    }

    public synchronized V put(K key, V value) {
        // if not in cache than create new cache node (acquire node from object pool)
        CacheNode<K, V> cacheNode = cache.containsKey(key) ? cache.get(key) :
                cacheNodePool.acquire(node -> node.clear());

        cacheNode.key = key;
        cacheNode.value = value;

        cache.put(key, cacheNode);

        // move to head if not in head
        moveToHead(cacheNode);

        // remove last if cache oversize
        removeTail();

        return cacheNode.value;
    }

    public synchronized V get(K key) {
        // if not in cache then return null
        if (!cache.containsKey(key))
            return null;

        // get node and move it to cache head
        CacheNode<K, V> cacheNode = cache.get(key);
        moveToHead(cacheNode);
        return cacheNode.value;
    }

    // internal ==============================================================================
    private void removeTail() {
        // if no tail then nothing to remove
        if (tail == null)
            return;

        // if below cache size then no need to remove
        if (cache.size() <= maxNumItems)
            return;

        // remove from cache and release pool object for reuse
        cache.remove(tail.key);
        cacheNodePool.release(tail, node -> node.clear());

        CacheNode<K, V> prev = tail.prev;

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

        private CacheNode<K, V> clear() {
            this.prev = null;
            this.next = null;
            this.key = null;
            this.value = null;
            return this;
        }
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
