package org.space.hulu.cache.common;

import org.space.hulu.SystemParams;
import org.space.hulu.config.CommonConfig;

public class CacheConfigImpl implements CacheConfig {

	private CommonConfig config;
	private volatile static CacheConfig instance;

	public static CacheConfig getInstance(CommonConfig config) {
		if (instance != null)
			return instance;

		synchronized (CacheConfigImpl.class) {
			if (instance != null)
				return instance;

			instance = new CacheConfigImpl(config);
			return instance;
		}

	}

	private CacheConfigImpl(CommonConfig config) {
		this.config = config;

	}

	@Override
	public boolean isEnableFileContentCache() {
 		return config.getBoolean(SystemParams.Cache.FileContentCache.ISALIVE,
				true);
	}

	@Override
	public long getMaxSizeForFileContentCache() {
		return config.getLong(
				SystemParams.Cache.FileContentCache.MAX_CACHE_SIZE,
				CacheConstants.DEFAULT_MAX_CACHE_SIZE);
		 
	}

	@Override
	public int getInitialNumberCapacityForFileContentCache() {
		return config
				.getInt(SystemParams.Cache.FileContentCache.INITIAL_CACHE_NUMBER_CAPACITY,
						CacheConstants.DEFAULT_INITIAL_CACHE_NUMBER_CAPACITY);
 	}

	@Override
	public int getInitialNumberCapacityForFilePointerCache() {
		return config
				.getInt(SystemParams.Cache.FilePointerCache.INITIAL_CACHE_NUMBER_CAPACITY,
						CacheConstants.DEFAULT_INITIAL_CACHE_NUMBER_CAPACITY);
 	}

}
