package org.space.hulu.cache;

/**
 * action define for memory size LRU cache. the size is cache object's size.
 * @author fu.jian
 *
 * @param <K>
 * @param <V>
 */
public interface SizeLruCache<K, V> extends Cache<K,V>{

	long getCurrentSize();

	void setCurrentSize(long currentSize);

	long getMaxSize();
	
	void changeCurrentSize(long delta);
  	
}