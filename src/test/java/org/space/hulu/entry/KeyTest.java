package org.space.hulu.entry;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Class Description
 *
 * @author Denny Ye
 * @since 2012-6-6
 * @version 1.0
 */
public class KeyTest {

	@Test
	public void verifyKey() {
		Assert.assertTrue(Key.isRegularKey("you-HG02"));
	}
	
	@Test
	public void getIdFromKey() {
		Assert.assertEquals(2, Key.getIdFromKey("you-HG02"));
	}
	
	@Test
	public void parseInitKey() {
		Assert.assertEquals("you", Key.getInitKey("you-HG02"));
	}
}

