package org.space.hulu.cache.common;

public interface CacheConfig {

	public abstract int getInitialNumberCapacityForFilePointerCache();

	public abstract int getInitialNumberCapacityForFileContentCache();

	public abstract long getMaxSizeForFileContentCache();

	public abstract boolean isEnableFileContentCache();
	 
}
