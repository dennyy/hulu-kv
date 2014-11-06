package org.space.hulu.performance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.space.hulu.HuluClient;
import org.space.hulu.cache.FileStorageCache;

/**
 * @author Denny Ye
 *
 */
public class CachePerfTest {
	
	int[] sizeScope = new int[]{100, 1024, 16 * 1024, 1 << 20};
	
	private String prefix = "perf_";
	private List<String> keyList = new ArrayList<String>();
	
	FileStorageCache fileStorage;
	
	@Before
	public void writeData() throws IOException {
		for (int i = 0; i < 100; i++) {
			fileStorage.storePacket(prefix + i,  null);
		}
	}
	
	@Test
	public void testTime() {
		
	}
	
	@After
	public void deleteData() throws IOException {
		for (String key : keyList) {
		}
	}

}
