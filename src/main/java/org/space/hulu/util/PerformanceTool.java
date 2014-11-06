package org.space.hulu.util;

import java.util.concurrent.TimeUnit;

public class PerformanceTool {

	private static final String PERFORMANCE_COST_FORMAT = "[performance][cost][in milliseconds:{%d}][in %s:{%d}][%s]";
	
	private long startTime;
	private TimeUnit timeUnit;

	public static PerformanceTool getInstance(TimeUnit timeUnit) {
		return new PerformanceTool(timeUnit);
	}

	public static PerformanceTool getInstance() {
		return new PerformanceTool(TimeUnit.MILLISECONDS);
	}

	private PerformanceTool(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public void start() {
		startTime = System.currentTimeMillis();
	}

	public String end(String opertionMsg) {
		long delta = System.currentTimeMillis() - startTime;
		String unit = timeUnit.toString().toLowerCase();
		long convertedDelta = timeUnit.convert(delta, TimeUnit.MILLISECONDS);
		String printContent = String.format(PERFORMANCE_COST_FORMAT, delta,
				unit, convertedDelta, opertionMsg);

		return printContent;
	}

}
