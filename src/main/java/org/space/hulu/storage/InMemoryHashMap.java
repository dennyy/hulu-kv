package org.space.hulu.storage;

import java.util.HashMap;

import org.space.hulu.cache.Cache;
import org.space.hulu.cache.impl.ConcurrentCacheImpl;
import org.space.hulu.entry.storage.FilePointer;

/**
 * Class Description
 * 
 * @author Denny Ye
 * @since 2012-5-18
 * @version 1.0
 */
public class InMemoryHashMap {

	private static final Cache<String, FilePointer> keyToFileMap = ConcurrentCacheImpl
			.getInstance();

	public static FilePointer get(String key) {
		return keyToFileMap.get(key);
	}

	public static void put(String key, FilePointer pointer) {
		keyToFileMap.put(key, pointer);
	}

}
