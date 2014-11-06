package org.space.hulu.cache.gc;

public class NativeHeapGcStrategyFactory {

	private static NativeHeapGcStrategyFactory instance = new NativeHeapGcStrategyFactory();

	public static NativeHeapGcStrategyFactory getInstance() {
		return instance;
	}

	public NativeHeapGcStrategy getCommonStrategy() {
		return new CommonNativeHeapGcStrategy();
	}
	
	public NativeHeapGcStrategy getNoGcStrategy() {
		return new NonNativeHeapGcStrategy();
	}

}
