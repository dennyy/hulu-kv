package org.space.hulu.cache.gc;

import java.nio.ByteBuffer;

import sun.misc.Cleaner;

@SuppressWarnings("restriction")
public class CommonNativeHeapGcStrategy implements NativeHeapGcStrategy {

	@Override
	public void gc(ByteBuffer directBuffer) {
		if (directBuffer != null) {
			if (directBuffer instanceof sun.nio.ch.DirectBuffer) {
				Cleaner cleaner = ((sun.nio.ch.DirectBuffer) directBuffer)
						.cleaner();
				if (cleaner != null)
					cleaner.clean();
			}
		}
 	}
}
