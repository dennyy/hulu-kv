package org.space.hulu.storage.file;

import java.io.IOException;

import org.junit.Test;

/**
 * @Author Denny Ye
 * @Since 2012-7-4
 */
public class FileMergerTest {

	@Test
	public void merge() throws IOException {
		FileMerger.merge("D:\\hulu\\data\\HG3322_data_0", "D:\\hulu\\data\\HG3322_data_0", "D:\\hulu\\data\\target");
	}
}


