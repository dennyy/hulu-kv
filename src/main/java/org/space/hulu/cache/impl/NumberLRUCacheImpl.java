package org.space.hulu.cache.impl;

import java.util.Map.Entry;

import org.space.hulu.cache.AbstractLRUCache;

/**
 * LRU cache . use cache count to take LRU.
 * @author fu.jian
 *
 * @param <K>
 * @param <V>
 */
public class NumberLRUCacheImpl<K, V> extends AbstractLRUCache<K, V> {

 	/**
	 * max cache object numbers
	 */
	private final int maxCacheNumber;

	private NumberLRUCacheImpl(int initialCacheNumberCapacity,int maxCacheNumber) {
		super(initialCacheNumberCapacity);
		this.maxCacheNumber = maxCacheNumber;
	}

	public static <K, V> NumberLRUCacheImpl<K, V> getInstance(int initialCacheNumberCapacity,int maxCacheNumber) {
		return new NumberLRUCacheImpl<K, V>(initialCacheNumberCapacity,maxCacheNumber);
	}

	@Override
	protected boolean isRemoveOldCache(Entry<K, V> eldest) {
		return size() > maxCacheNumber;
 	}

}
