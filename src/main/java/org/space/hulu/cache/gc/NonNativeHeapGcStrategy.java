package org.space.hulu.cache.gc;

import java.nio.ByteBuffer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class NonNativeHeapGcStrategy implements  NativeHeapGcStrategy {

	private final static Logger LOGGER= LogManager.getLogger(NonNativeHeapGcStrategy.class);
	
	public NonNativeHeapGcStrategy() {
  	}
	
	@Override
	public void gc(ByteBuffer directBuffer) {
		if(LOGGER.isDebugEnabled())
			LOGGER.debug("[cache][gc]no need gc");
	}

}
