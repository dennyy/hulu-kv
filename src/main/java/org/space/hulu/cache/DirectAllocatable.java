package org.space.hulu.cache;

import java.nio.ByteBuffer;

/**
 * actions support allocate memory directly out of heap.
 * @author fu.jian
 *
 */
public interface DirectAllocatable {

	/**
	 * get data from byte buffer
	 * @param byteBuffer
	 * @return byte[]
	 */
	byte[] fromByteBuffer(ByteBuffer byteBuffer);

	/**
	 * store byte[] to byte buffer
	 * @param value
	 * @return byteBuffer
	 */
	ByteBuffer toByteBuffer(byte[] value);

}