package org.space.hulu.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.space.hulu.util.DigestUtil.DigestAlgorithm;

public class DigestUtilTest {
	
	
	@Test
	public void testString() {
 		try {
 			String digest = DigestUtil.getDigest(RandomStringUtils.random(10).getBytes(), DigestAlgorithm.SHA_1);
			Assert.assertNotNull(digest);
			System.out.println(digest);
		} catch (Exception e) {
			
			Assert.fail();
 		}
		 
	}
	
	@Test
	public void testFilePath() throws IOException {
		
		File createTempFile = File.createTempFile(RandomStringUtils.random(12), ".txt");
 		try {
 			
  			String digest = DigestUtil.getDigest(createTempFile.getAbsolutePath());
			Assert.assertNotNull(digest);
			System.out.println(digest);
		} catch (Exception e) {
			
			Assert.fail();
 		}finally{
// 			if(createTempFile!=null)
// 				createTempFile.delete();
 		}
		 
	}
	
	@Test
	public void testStringWithDifferentAlgorithm() {
 		try {
 			String digest = DigestUtil.getDigest(RandomStringUtils.random(10).getBytes(), DigestAlgorithm.MD5);
			Assert.assertNotNull(digest);
			System.out.println(digest);
		} catch (Exception e) {
			
			Assert.fail();
 		}
		 
	}

}
