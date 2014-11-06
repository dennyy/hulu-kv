package org.space.hulu.cache.info;

public class FilePointerCacheInfo extends CacheInfo {

	public static FilePointerCacheInfo getInstance(long initailCacheNumberCapacity) {
		return new FilePointerCacheInfo(initailCacheNumberCapacity);
	}

	public static FilePointerCacheInfo getInstance(long initailCacheNumberCapacity,
			long cacheNumbers) {
		return new FilePointerCacheInfo(initailCacheNumberCapacity, cacheNumbers);
	}

	private FilePointerCacheInfo(long initailCacheNumberCapacity) {
		super(initailCacheNumberCapacity);
	}
	
	private FilePointerCacheInfo(long initailCacheNumberCapacity, long cacheNumbers) {
		super(initailCacheNumberCapacity, cacheNumbers);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FilePointerCacheInfo [cacheNumbers=");
		builder.append(cacheNumbers);
		builder.append(", initailCacheNumberCapacity=");
		builder.append(initailCacheNumberCapacity);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public FilePointerCacheInfoReport generateReport() {
		return  new FilePointerCacheInfoReport(getCacheNumbers(),
				getInitailCacheNumberCapacity());
	} 
	
}
