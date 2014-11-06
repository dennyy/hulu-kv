package org.space.hulu.cache.impl;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.space.hulu.cache.AbstactNativeHeapCache;
import org.space.hulu.cache.AbstractSizeLRUCache;
import org.space.hulu.cache.SizeLruCache;
import org.space.hulu.cache.common.CacheConstants;
import org.space.hulu.cache.gc.NativeHeapGcStrategy;
import org.space.hulu.cache.gc.NativeHeapGcStrategyFactory;

/**
 * design for cache out of JVM heap. The cache is LRU for cache's object's size.
 * 
 * @author fu.jian
 *
 * @param <K>
 */
public class NativeHeapCacheImpl<K> extends AbstactNativeHeapCache<K> {

	private static final NativeHeapGcStrategy DEFALUT_GC_STRATEGY = NativeHeapGcStrategyFactory.getInstance().getCommonStrategy();
	
	private SizeLruCache<K, ByteBuffer> cache;
	private final NativeHeapGcStrategy gcStrategy;


	private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
	private final ReadLock readLock = reentrantReadWriteLock.readLock();
	private final WriteLock writeLock = reentrantReadWriteLock.writeLock();

	private NativeHeapCacheImpl(int initialCacheNumberCapacity, long maxSize, NativeHeapGcStrategy outOfHeapGcStrategy) {
		super(maxSize);
		AbstractSizeLRUCache<K, ByteBuffer> abstractSizeLRUCache = new AbstractSizeLRUCache<K, ByteBuffer>(
				initialCacheNumberCapacity, maxSize) {

			/*
			 * get size for oldest entry: in this position.it's bytebuffer's
			 * capacity due to write full data into it.
			 * 
			 * @see
			 * org.space.hulu.cache.AbstractSizeLRUCache#getSizeForEntry(java
			 * .util.Map.Entry)
			 */
			@Override
			protected long getSizeForEntry(Entry<K, ByteBuffer> eldest) {
				return eldest.getValue().capacity();
			}
 
			@Override
			protected void gcEldest(Entry<K, ByteBuffer> eldest) {
 				 gcStrategy.gc(eldest.getValue());
			}

		};

		abstractSizeLRUCache.setLock(false);
		this.cache = abstractSizeLRUCache;
		this.gcStrategy=outOfHeapGcStrategy;

	}
	
	private NativeHeapCacheImpl(int initialCacheNumberCapacity, long maxSize){
		this( initialCacheNumberCapacity,  maxSize, DEFALUT_GC_STRATEGY);
	}

	private NativeHeapCacheImpl(long maxSize, SizeLruCache<K, ByteBuffer> cache, NativeHeapGcStrategy outOfHeapGcStrategy) {
		super(maxSize);
		this.cache = cache;
		this.gcStrategy=outOfHeapGcStrategy;
	}

	public static <K> NativeHeapCacheImpl<K> getInstance(int initialCacheNumberCapacity,
			long maxSize) {
		return new NativeHeapCacheImpl<K>(initialCacheNumberCapacity, maxSize);
	}
	
	public static <K> NativeHeapCacheImpl<K> getInstance(int initialCacheNumberCapacity,
			long maxSize,NativeHeapGcStrategy outOfHeapGcStrategy) {
		return new NativeHeapCacheImpl<K>(initialCacheNumberCapacity, maxSize, outOfHeapGcStrategy);
	}
	
	public static <K> NativeHeapCacheImpl<K> getInstance() {
		return new NativeHeapCacheImpl<K>(CacheConstants.DEFAULT_INITIAL_CACHE_NUMBER_CAPACITY, CacheConstants.DEFAULT_MAX_CACHE_SIZE);
	}

	public static <K> NativeHeapCacheImpl<K> getInstance(long maxSize,
			SizeLruCache<K, ByteBuffer> cache) {
		return new NativeHeapCacheImpl<K>(maxSize, cache, DEFALUT_GC_STRATEGY);
	}

	@Override
	public byte[] get(K key) {		
		ByteBuffer byteBufferInCache; 		
		
		lockRead();		
		try {
			 byteBufferInCache = cache.get(key);
 		} finally {
			unLockRead();
		}
		
  		return byteBufferInCache==null? null:fromByteBuffer(byteBufferInCache);
	}

	private void unLockRead() {
		readLock.unlock();
	}

	private void lockRead() {
		readLock.lock();
	}

	@Override
	public boolean containsKey(K key) {
		lockRead();
		try {
			return cache.containsKey(key);
		} finally {
			unLockRead();
		}
	}

	@Override
	public byte[] put(K key, byte[] value) {
		ByteBuffer allocateDirect = toByteBuffer(value);

		lockWrite();
		try {
			ByteBuffer oldValue = cache.put(key, allocateDirect);
			// increase size no matter if replace old value
			increaseSize(value);

			// if replace the old value. reduce the size for old value.
			if (isReplaceOldValue(oldValue)) {
				reduceSize(oldValue.capacity());
 				byte[] oldValueInByteArray = fromByteBuffer(oldValue);
 				
				gcStrategy.gc(oldValue);
				oldValue=null;	

				return oldValueInByteArray;
			}

			// keep logical with hashmap. no replace return null indicate no
			// update happen
			return null;
		} finally {
			unLockWrite();
		}
	}
  
	private boolean isReplaceOldValue(ByteBuffer oldValue) {
		return oldValue != null;
	}
  
	@Override
	public void changeCurrentSize(long delta) {
		cache.changeCurrentSize(delta);
	}
	 

	@Override
	public void putIfAbsent(K key, byte[] value) {
		lockWrite();
		try {
			if (!cache.containsKey(key))
				put(key, value);
		} finally {
			unLockWrite();
		}

	}

	private void unLockWrite() {
		writeLock.unlock();
	}

	private void lockWrite() {
		writeLock.lock();
	}

	@Override
	public boolean containsValue(byte[] value) {
		ByteBuffer allocateDirect = toByteBuffer(value);
		lockRead();
		try {
			return cache.containsValue(allocateDirect);
		} finally {
			unLockRead();
		}
 	}

	@Override
	public void putAll(Map<? extends K, ? extends byte[]> m) {
 		for (Map.Entry<? extends K, ? extends byte[]> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		};

	}

	@Override
	public void remove(K key) {
		lockWrite();
		try {
			ByteBuffer byteBufferInCache = cache.get(key);
			if(byteBufferInCache!=null){
				reduceSize(byteBufferInCache.capacity());
				gcStrategy.gc(byteBufferInCache);
  				cache.remove(key);
  				byteBufferInCache=null;
  			}
 		} finally {
			unLockWrite();
		}
	}

	@Override
	public void clear() {
		lockWrite();
		try {
			cache.clear();
			cache.setCurrentSize(0);
		} finally {
			unLockWrite();
		}

	}

	@Override
	public int size() {
		lockRead();
		try {
			return cache.size();
		} finally {
			unLockRead();
		}

	}

	@Override
	public boolean isEmpty() {
		lockRead();
		try {
			return cache.isEmpty();
		} finally {
			unLockRead();
		}

	}
  
	@Override
	public long getCurrentSize() {
 		return cache.getCurrentSize();
 
	}

	@Override
	public void setCurrentSize(long currentSize) { 
		cache.setCurrentSize(currentSize);
	}

	@Override
	public long getMaxSize() {
 		return cache.getMaxSize();
 	}
	

	public NativeHeapGcStrategy getGcStrategy() {
		return gcStrategy;
	}
 
}
