package org.space.hulu.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Class Description
 *
 * @author Denny Ye
 * @since 2012-5-25
 * @version 1.0
 */
public class ValidationTest {

	@Test
	public void testExeption() {
		try  {
			Validation.effectiveStr(null);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e != null);
		}
		
		try  {
			Validation.effectiveList(null);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e != null);
		}
		
		try  {
			Validation.effectiveData(null);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e != null);
		}
		
		try {
			Validation.isPositive(-2333);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e != null);
		}
	}
	
	@Test
	public void testRegular() {
		Validation.effectiveData("aa".getBytes());
		Validation.effectiveStr("aa");
		List<Integer> list = new ArrayList<Integer>();
		list.add(11);
		
		Validation.effectiveList(list);
	}
	
	@Test
	public void testJudgment() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(11);
		Assert.assertTrue(Validation.isEffectiveList(list));
		
		Assert.assertTrue(Validation.isEffectiveStr("aa"));
	}
}

