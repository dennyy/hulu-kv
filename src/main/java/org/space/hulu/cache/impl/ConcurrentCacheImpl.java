package org.space.hulu.cache.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.space.hulu.cache.Cache;
import org.space.hulu.cache.common.CacheConstants;

/**
 * design for common cache(not take LRU operation). it is thread safe.
 * To avoid resize for cache capacity. the initial size be set same with cache number.
 * and the load factor be set to 1.0.
 * 
 * @author fu.jian
 *
 * @param <K>
 * @param <V>
 */
public class ConcurrentCacheImpl<K, V> implements Cache<K, V> {
 
	private ConcurrentCacheImpl(final int initialCacheNumberCapacity) {
 		this.concurrentHashMap = new ConcurrentHashMap<K, V>(initialCacheNumberCapacity,
				CacheConstants.DEFAULT_LOAD_FACTOR);
	}
	
	private ConcurrentCacheImpl() {
		this(CacheConstants.DEFAULT_INITIAL_CACHE_NUMBER_CAPACITY); 
	}
	
	/**
	 * as defaut. the initial size is {@link CacheConstants#DEFAULT_INITIAL_CACHE_NUMBER_CAPACITY}
	 * @return
	 */
	public static <K, V> ConcurrentCacheImpl<K, V> getInstance() {
		return new ConcurrentCacheImpl<K, V>();
	}

	public static <K, V> ConcurrentCacheImpl<K, V> getInstance(int maxCapacity) {
		return new ConcurrentCacheImpl<K, V>(maxCapacity);
	}

	private ConcurrentHashMap<K, V> concurrentHashMap;

	public boolean isEmpty() {
		return concurrentHashMap.isEmpty();
	}

	public int size() {
		return concurrentHashMap.size();
	}

	public V get(K key) {
		return concurrentHashMap.get(key);
	}

	public boolean containsKey(K key) {
		return concurrentHashMap.containsKey(key);
	}

	public boolean containsValue(V value) {
		return concurrentHashMap.containsValue(value);
	}

	public V put(K key, V value) {
		return concurrentHashMap.put(key, value);
	}

	public void putIfAbsent(K key, V value) {
		concurrentHashMap.putIfAbsent(key, value);
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		concurrentHashMap.putAll(m);
	}

	public void remove(K key) {
		concurrentHashMap.remove(key);
	}

	public void clear() {
		concurrentHashMap.clear();
	}

}
