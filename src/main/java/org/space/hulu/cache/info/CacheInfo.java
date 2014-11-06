package org.space.hulu.cache.info;


public abstract class CacheInfo implements Reportable{
	
 	protected volatile long cacheNumbers;
	protected final long initailCacheNumberCapacity;
	 
	public CacheInfo(long initailCacheNumberCapacity) {
		super();
		this.initailCacheNumberCapacity = initailCacheNumberCapacity;
	}
	
	public CacheInfo(long initailCacheNumberCapacity, long cacheNumbers) {
		super();
		this.cacheNumbers=cacheNumbers;
		this.initailCacheNumberCapacity = initailCacheNumberCapacity;
	}

	public long getCacheNumbers() {
		return cacheNumbers;
	}

	public void setCacheNumbers(long cacheNumbers) {
		this.cacheNumbers = cacheNumbers;
	}

	public long getInitailCacheNumberCapacity() {
		return initailCacheNumberCapacity;
	}

	@Override
	public String toString() {
		return "BaseCacheInfo [cacheNumbers=" + cacheNumbers
				+ ", initailCacheNumberCapacity=" + initailCacheNumberCapacity + "]";
	}
  
}
