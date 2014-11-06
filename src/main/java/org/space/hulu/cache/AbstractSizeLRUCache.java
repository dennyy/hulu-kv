package org.space.hulu.cache;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.space.hulu.util.Validation;

/**
 * Cache for calcute whole cache objects size. As default. it's not take LRU due
 * to maxSize > currentSize;
 * 
 * @author fu.jian
 * @date May 24, 2012
 * @param <K>
 * @param <V>
 */
public abstract class AbstractSizeLRUCache<K, V> extends AbstractLRUCache<K, V>
		implements SizeLruCache<K, V> {

	private AtomicLong currentSize=new AtomicLong(0);
  
	/**
	 * memory max allow occupied size.
	 */
	private final long maxSize;

	/**
	 * @param initialCount: initial cache count
	 * @param maxSize: 
	 */
	protected AbstractSizeLRUCache(int initialCacheNumberCapacity,long maxSize) {
		super(initialCacheNumberCapacity);
		Validation.isPositive(maxSize); 
		this.maxSize = maxSize;
	}

	@Override
	protected boolean isRemoveOldCache(Entry<K, V> eldest) {
		if (getCurrentSize() > maxSize) {
			long delta=0-getSizeForEntry(eldest);
			this.currentSize.getAndAdd(delta);
			gcEldest(eldest);
 			return true;
		}
 		return false;
	}

	protected abstract void gcEldest(Entry<K, V> eldest);

 	protected abstract long getSizeForEntry(Entry<K, V> eldest);

	public long getCurrentSize() {
		return currentSize.get();
	}

	public void setCurrentSize(long currentSize) {
		this.currentSize.set(currentSize);
	}
	
	public void changeCurrentSize(long delta) {
		this.currentSize.getAndAdd(delta);
	}

	public long getMaxSize() {
		return maxSize;
	}
	 
}
