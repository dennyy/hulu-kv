package org.space.hulu.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Class Description
 *
 * @author Denny Ye
 * @since 2012-5-25
 * @version 1.0
 */
public class ChecksumUtilsTest {

	@Test
	public void testVerify() {
		byte[] target = "youareok".getBytes();
		
		byte[] checksum = ChecksumUtils.getChecksum(target);
		Assert.assertTrue(ChecksumUtils.verifyChunksum(target, checksum));
	}
	
	@Test
	public void testException() {
		try {
			ChecksumUtils.getChecksum(null);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e != null);
		}
	}
}

