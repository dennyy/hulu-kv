package org.space.hulu.cache.info;


public class CacheInfoReport {
	 
	protected long cacheNumbers;
	protected long initailCacheNumberCapacity;
	protected double cacheNumberRatio;
 	
 	public CacheInfoReport(){
 		
 	}
 	 
	public CacheInfoReport(long cacheNumbers,
			long initailCacheCapacity) {
		super();
		this.cacheNumbers = cacheNumbers;
		this.initailCacheNumberCapacity = initailCacheCapacity;
 		this.cacheNumberRatio=(double)cacheNumbers/initailCacheCapacity;
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

	public void setInitailCacheNumberCapacity(long initailCacheNumberCapacity) {
		this.initailCacheNumberCapacity = initailCacheNumberCapacity;
	}

	public double getCacheNumberRatio() {
		return cacheNumberRatio;
	}

	public void setCacheNumberRatio(double cacheNumberRatio) {
		this.cacheNumberRatio = cacheNumberRatio;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CacheInfoReport [cacheNumbers=");
		builder.append(cacheNumbers);
		builder.append(", initailCacheNumberCapacity=");
		builder.append(initailCacheNumberCapacity);
		builder.append(", cacheNumberRatio=");
		builder.append(cacheNumberRatio);
		builder.append("]");
		return builder.toString();
	}
  
	
	
	
}
