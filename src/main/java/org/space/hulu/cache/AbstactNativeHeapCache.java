package org.space.hulu.cache;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstactNativeHeapCache<K> implements
		SizeLruCache<K, byte[]>, DirectAllocatable {

	/**
	 * max memory allow size for cache object
	 */
	private final long maxSize;
	/**
	 * 
	 * store cache object's overall size
	 */
	private AtomicLong currentSize = new AtomicLong(0);

	protected AbstactNativeHeapCache(long maxSize) {
		this.maxSize = maxSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.space.hulu.cache.DirectAllocatable#fromByteBuffer(java.nio.ByteBuffer
	 * )
	 */
	public byte[] fromByteBuffer(ByteBuffer byteBuffer) {
		byte[] dst = new byte[byteBuffer.capacity()];
		byteBuffer.duplicate().get(dst);

		return dst;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.space.hulu.cache.DirectAllocatable#toByteBuffer(byte[])
	 */
	public ByteBuffer toByteBuffer(byte[] value) {
		ByteBuffer allocateDirect = ByteBuffer.allocateDirect(value.length);
		allocateDirect.put(value);
		allocateDirect.flip();

		return allocateDirect;
	}

	public long getCurrentSize() {
		return currentSize.get();
	}

	public void setCurrentSize(long currentSize) {
		this.currentSize.set(currentSize);
	}

	public long getMaxSize() {
		return maxSize;
	}

	/**
	 * reduce current size after reduce cache object or replace cache object
	 * 
	 * @param value
	 */
	protected void reduceSize(byte[] value) {
		if (value != null)
			changeCurrentSize(0 - value.length);
	}

	protected void reduceSize(int capacity) {
		changeCurrentSize(0 - capacity);

	}

	/**
	 * add current size after put new cache object
	 * 
	 * @param value
	 */
	protected void increaseSize(byte[] value) {
		if (value != null)
			changeCurrentSize(value.length);
	}

	public void changeCurrentSize(long delta) {
		this.currentSize.getAndAdd(delta);
	}

}
