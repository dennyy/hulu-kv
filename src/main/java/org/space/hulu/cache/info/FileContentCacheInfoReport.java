package org.space.hulu.cache.info;

import java.math.BigDecimal;

public class FileContentCacheInfoReport extends CacheInfoReport {

	private static final BigDecimal BIG_DECLIMAL_ZERO = BigDecimal.ZERO;

	private boolean isAlive;

	private BigDecimal cacheMiss=BIG_DECLIMAL_ZERO;
	private BigDecimal cacheHit=BIG_DECLIMAL_ZERO;
	private BigDecimal cacheAccess=BIG_DECLIMAL_ZERO;
	private double cacheHitRatio;

	private long totalSpace;
	private long usedSpace;
	private double usedSpaceRatio;


	public FileContentCacheInfoReport() {
		super();
	}

	public FileContentCacheInfoReport(boolean isAlive, BigDecimal cacheHit,
			BigDecimal cacheMiss, long usedSpace, long totalSpace,
			long cacheNumbers, long initailCacheNumberCapacity) {
		super(cacheNumbers, initailCacheNumberCapacity);
		this.isAlive = isAlive;
		this.cacheHit = cacheHit;
		this.cacheMiss = cacheMiss;
		this.usedSpace = usedSpace;
		this.totalSpace = totalSpace;

		this.cacheAccess = cacheHit.add(cacheMiss);
		if(!cacheAccess.equals(BIG_DECLIMAL_ZERO))
			this.cacheHitRatio = cacheHit.divide(cacheAccess).doubleValue(); 
		this.usedSpaceRatio= (double)usedSpace/totalSpace;
	}
 
	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public BigDecimal getCacheMiss() {
		return cacheMiss;
	}

	public void setCacheMiss(BigDecimal cacheMiss) {
		this.cacheMiss = cacheMiss;
	}

	public BigDecimal getCacheHit() {
		return cacheHit;
	}

	public void setCacheHit(BigDecimal cacheHit) {
		this.cacheHit = cacheHit;
	}

	public BigDecimal getCacheAccess() {
		return cacheAccess;
	}

	public void setCacheAccess(BigDecimal cacheAccess) {
		this.cacheAccess = cacheAccess;
	}

	public double getCacheHitRatio() {
		return cacheHitRatio;
	}

	public void setCacheHitRatio(double cacheHitRatio) {
		this.cacheHitRatio = cacheHitRatio;
	}

	public long getTotalSpace() {
		return totalSpace;
	}

	public void setTotalSpace(long totalSpace) {
		this.totalSpace = totalSpace;
	}

	public long getUsedSpace() {
		return usedSpace;
	}

	public void setUsedSpace(long usedSpace) {
		this.usedSpace = usedSpace;
	}

	public double getUsedSpaceRatio() {
		return usedSpaceRatio;
	}

	public void setUsedSpaceRatio(double usedSpaceRatio) {
		this.usedSpaceRatio = usedSpaceRatio;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FileContentCacheInfoReport [isAlive=");
		builder.append(isAlive);
		builder.append(", cacheMiss=");
		builder.append(cacheMiss);
		builder.append(", cacheHit=");
		builder.append(cacheHit);
		builder.append(", cacheAccess=");
		builder.append(cacheAccess);
		builder.append(", cacheHitRatio=");
		builder.append(cacheHitRatio);
		builder.append(", usedSpace=");
		builder.append(usedSpace);
		builder.append(", totalSpace=");
		builder.append(totalSpace);
		builder.append(", usedSpaceRatio=");
		builder.append(usedSpaceRatio);
		builder.append(", cacheNumbers=");
		builder.append(cacheNumbers);
		builder.append(", initailCacheNumberCapacity=");
		builder.append(initailCacheNumberCapacity);
		builder.append(", cacheNumberRatio=");
		builder.append(cacheNumberRatio);
		builder.append("]");
		return builder.toString();
	}
	 
}
