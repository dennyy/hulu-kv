package org.space.hulu.cache.common;

public class CacheConstants {

	public static final int DEFAULT_LOAD_FACTOR = 1;

	/**
	 * default: {@value DEFAULT_INITIAL_CACHE_NUMBER_CAPACITY} 
	 * Use this constants to avoid resize cache operation.
	 */
	public static final int DEFAULT_INITIAL_CACHE_NUMBER_CAPACITY = 100 * 1024;

	/**
	 * default: 256M 
	 * <li><b>-XX:MaxDirectMemorySize=100M </b>:as default native heap use same size with java heap(-Xmx), so should change the size;
	 * <li><b>-d64</b> :load 64 bit jvm to allow allocated size can bigger than 4G(or less):
	 */
	public static final long DEFAULT_MAX_CACHE_SIZE = 256L * 1024 * 1024;

}
