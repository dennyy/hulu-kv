package org.space.hulu.cache;

import java.util.Map;

public interface Cache<K, V> {

	V get(K key);

	boolean containsValue(V value);

	boolean containsKey(K key);

	V put(K key, V value);

	void putAll(Map<? extends K, ? extends V> m);

	void remove(K key);

	void clear();

	int size();

	boolean isEmpty();

	void putIfAbsent(K key, V value);

}