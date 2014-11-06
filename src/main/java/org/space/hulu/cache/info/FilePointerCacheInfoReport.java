package org.space.hulu.cache.info;

public class FilePointerCacheInfoReport extends CacheInfoReport {

	public FilePointerCacheInfoReport(long cacheNumbers,
			long initailCacheNumberCapacity) {
		super(cacheNumbers, initailCacheNumberCapacity);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FilePointerCacheInfoReport [cacheNumbers=");
		builder.append(cacheNumbers);
		builder.append(", initailCacheNumberCapacity=");
		builder.append(initailCacheNumberCapacity);
		builder.append(", cacheNumberRatio=");
		builder.append(cacheNumberRatio);
		builder.append("]");
		return builder.toString();
	}
	
	
	 
}
