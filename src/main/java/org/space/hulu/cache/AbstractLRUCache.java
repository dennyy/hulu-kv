package org.space.hulu.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.space.hulu.cache.common.CacheConstants;

public abstract class AbstractLRUCache<K, V> implements Cache<K, V> {
  
	/**
	 * set initial count in cache to avoid resize operation 
	 */
	private int initialCacheNumberCapacity; 
	private LinkedHashMap<K, V> cache;
	private boolean isLock = true;;

	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}

	private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
	private final ReadLock readLock = reentrantReadWriteLock.readLock();
	private final WriteLock writeLock = reentrantReadWriteLock.writeLock();

	protected AbstractLRUCache() {
		this(CacheConstants.DEFAULT_INITIAL_CACHE_NUMBER_CAPACITY);
	}

	protected AbstractLRUCache(final int initialCacheNumberCapacity) {
		this.initialCacheNumberCapacity = initialCacheNumberCapacity;
		this.cache = new LinkedHashMap<K, V>(initialCacheNumberCapacity,
				CacheConstants.DEFAULT_LOAD_FACTOR) {

			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Entry<K, V> eldest) {
				return isRemoveOldCache(eldest);
			}

		};
	}

	protected abstract boolean isRemoveOldCache(Entry<K, V> eldest);
	
	public int getInitialCacheNumberCapacity() {
		return initialCacheNumberCapacity;
	}
 
 	public V get(Object key) {
		lockRead();
		try {
			return cache.get(key);
		} finally {
			unlockRead();
		}
	}

	private void unlockRead() {
		if (isLock)
			readLock.unlock();
	}

	private void lockRead() {
		if (isLock)
			readLock.lock();
	}

	public boolean containsValue(V value) {
		lockRead();
		try {
			return cache.containsValue(value);
		} finally {
			unlockRead();
		}
	}

	public boolean containsKey(K key) {
		lockRead();
		try {
			return cache.containsKey(key);
		} finally {
			unLockWrite();
		}
	}

	public int size() {
		lockRead();
		try {
			return cache.size();
		} finally {
			unlockRead();
		}
	}

	public boolean isEmpty() {
		lockRead();
		try {
			return cache.isEmpty();
		} finally {
			unlockRead();
		}
	}

	public V put(K key, V value) {
		lockWrite();
		try {
			return cache.put(key, value);
		} finally {
			unLockWrite();
		}
	}

	private void unLockWrite() {
		if (isLock)
			writeLock.unlock();
	}

	private void lockWrite() {
		if (isLock)
			writeLock.lock();
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		lockWrite();
		try {
			cache.putAll(m);
		} finally {
			unLockWrite();
		}
	}

	public void remove(K key) {
		lockWrite();
		try {
			cache.remove(key);
		} finally {
			unLockWrite();
		}
	}

	public void clear() {
		lockWrite();
		try {
			cache.clear();
		} finally {
			unLockWrite();
		}
	}

	@Override
	public void putIfAbsent(K key, V value) {
		lockWrite();
		try {
			if (!containsKey(key))
				cache.put(key, value);
		} finally {
			unLockWrite();
		}
	}

}