package org.space.hulu;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Class Description
 *
 * @author Denny Ye
 * @since 2012-5-25
 * @version 1.0
 */
public class HuluClientTest {

	HuluClient client = new HuluClient();
	
	
	public void write() throws IOException {
		String finalKey = client.put("yesterday32", "firstOne".getBytes(), 1);
		Assert.assertEquals("firstOne", new String(client.get(finalKey)));
	}
	
	@Test
	public void status() throws IOException {
		System.out.println(client.getStatus());
	}
	
	public void writeTwice() throws IOException {
		client.put("myKey", "firstOne".getBytes(), 1);
		client.put("myKey", "firstOne".getBytes(), 1);
	}
	
	public void exception() throws IOException {
		try {
			client.put(null);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e != null);
		}
		
		try {
			client.put(new byte[0]);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e != null);
		}
		
		try {
			byte[] data = new byte[1];
			
			client.put(data, -100);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e != null);
		}
		
		try {
			byte[] data = new byte[1];
			client.put(data, 100);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e != null);
		}
		
		try {
			byte[] data = new byte[1];
			client.put(data, 0);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e != null);
		}
		
		try {
			client.get("aaa");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e != null);
		}
	}
	
	public void amount() throws IOException {
		long start = System.currentTimeMillis();
		
		int total = 10000;
		for (int i = 0; i < total; i++) {
			byte[] data = new byte[1];
			client.put("key_" + i, data, 1);
		}
		
		long cost = System.currentTimeMillis() -  start;
		System.out.println("Cost:" + cost + ", Avg:" + (((double)total) / cost));
	}
	
	
	public void remove() throws IOException {
		String finalKey = client.put("myKey2", "SecondOne".getBytes(), 1);
		client.delete(finalKey);
		try {
			System.out.println("Final search:" + client.get(finalKey));
		} catch (IOException e) {
			System.out.println("That's OK!" + e.getMessage());
		}
	}
	
}

