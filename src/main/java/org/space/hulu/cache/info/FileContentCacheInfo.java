package org.space.hulu.cache.info;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

public class FileContentCacheInfo extends CacheInfo {

	private static final BigDecimal STEP = BigDecimal.ONE;

	private volatile BigDecimal cacheMiss = BigDecimal.ZERO;
	private volatile BigDecimal cacheHit = BigDecimal.ZERO;
	private AtomicLong usedSpace = new AtomicLong(0);

	private final long totalSpace;
	private final boolean isAlive;

	private final Object cacheMissLock = new Object();
	private final Object cacheHitLock = new Object();

	private FileContentCacheInfo(boolean isAlive, long totalSpace,
			long initailCacheNumberCapacity) {
		super(initailCacheNumberCapacity);
		this.isAlive = isAlive;
		this.totalSpace = totalSpace;
	}

	public static FileContentCacheInfo getInstance(boolean isAlive,
			long totalSpace, long initailCacheNumberCapacity) {
		return new FileContentCacheInfo(isAlive, totalSpace,
				initailCacheNumberCapacity);
	}

	public static FileContentCacheInfo getInstance(long totalSpace,
			long initailCacheNumberCapacity) {
		return new FileContentCacheInfo(true, totalSpace, initailCacheNumberCapacity);
	}

	public BigDecimal getCacheMiss() {
		return cacheMiss;
	}

	public BigDecimal getCacheHit() {
		return cacheHit;
	}

	public void increaseCacheMiss() {
		synchronized (cacheMissLock) {
			this.cacheMiss = this.cacheMiss.add(STEP);
		}
	}

	public void increaseCacheHit() {
		synchronized (cacheHitLock) {
			this.cacheHit = this.cacheHit.add(STEP);
		}
	}

	public long getTotalSpace() {
		return totalSpace;
	}

	public long getUsedSpace() {
		return usedSpace.get();
	}

	public void setUsedSpace(long usedSpace) {
		this.usedSpace.set(usedSpace);
	}

	public void changeUsedSpace(long delta) {
		this.usedSpace.addAndGet(delta);
	}

	public boolean getIsAlive() {
		return isAlive;
	}
   
 	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FileContentCacheInfo [isAlive=");
		builder.append(isAlive);
		builder.append(", totalSpace=");
		builder.append(totalSpace);
		builder.append(", initailCacheNumberCapacity=");
		builder.append(initailCacheNumberCapacity);
		builder.append("]");
		return builder.toString();
	}

	public FileContentCacheInfoReport generateReport() {
		return new FileContentCacheInfoReport(getIsAlive(), getCacheHit(),
				getCacheMiss(), getUsedSpace(), getTotalSpace(),
				getCacheNumbers(), getInitailCacheNumberCapacity());
	}

}
