package org.space.hulu.cache.gc;

import java.nio.ByteBuffer;
 
public interface NativeHeapGcStrategy {
 
	void gc(ByteBuffer directBuffer);

}
