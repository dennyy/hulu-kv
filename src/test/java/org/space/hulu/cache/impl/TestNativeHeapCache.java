package org.space.hulu.cache.impl;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.annotations.Test;

import junit.framework.Assert;

public class TestNativeHeapCache {

	@Test
	public void testSizeControlWhenUpdateCache() {
		NativeHeapCacheImpl<Integer> instance = NativeHeapCacheImpl.getInstance(
				100, 900);

		int key = 1;
		byte[] oldValue = UUID.randomUUID().toString().getBytes();
		instance.put(key, oldValue);
		System.out.println(instance.getCurrentSize());

		byte[] newValue = (UUID.randomUUID().toString() + "tmp").getBytes();

		instance.put(key, newValue);
		System.out.println(instance.getCurrentSize());

		Assert.assertNotSame(instance.getCurrentSize(), oldValue.length
				+ newValue.length);

	}
	
	
	@Test
	public void testMultiReadCache() throws InterruptedException {

		final String abc = "abcdefghigklmnabcdefghigklmnabcdefghigklmnabcdefghigklmnabcdefghigklmnabcdefghlmnabcdefghigklmnabcdefghigklmn";	 
		final NativeHeapCacheImpl<String> instance = NativeHeapCacheImpl.getInstance(100, 100000L);
		instance.put("key", abc.getBytes());
		
		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(10);
  		final CountDownLatch countDownLatch = new CountDownLatch(100);		

		for (int i = 0; i < 100; i++) {
			newFixedThreadPool.submit(new Runnable() {

				@Override
				public void run() {
 					try {

						byte[] bs = instance.get("key");
						System.out.println(new String(bs));
 						Assert.assertEquals(new String(bs), abc);
 					} catch (Exception e) {
						Assert.fail();
						e.printStackTrace();
					}
					
					countDownLatch.countDown();

				}

				private String getName() {
					return Thread.currentThread().getName();
				}
			});

		}
		
		countDownLatch.await();
		newFixedThreadPool.shutdownNow();
		

	}

	@Test
	public void testByteBufferMultiRead() throws InterruptedException {

		String abc = "abcdefghigklmnabcdefghigklmnabcdefghigklmnabcdefghigklmnabcdefghigklmnabcdefghlmnabcdefghigklmnabcdefghigklmn";
		byte[] bytes = abc.getBytes();
		final ByteBuffer allocateDirect = ByteBuffer
				.allocateDirect(bytes.length);
		allocateDirect.put(bytes);
		System.out.println(allocateDirect);
		allocateDirect.flip();

		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(100);		
		
		final CountDownLatch countDownLatch = new CountDownLatch(100);
		for (int i = 0; i < 100; i++) {
			newFixedThreadPool.submit(new Runnable() {

				@Override
				public void run() {

					try {

						ByteBuffer newByteBuffer = allocateDirect.duplicate();
						byte[] dst = new byte[newByteBuffer.capacity()];
						newByteBuffer.get(dst);

						System.out.println("end:" + newByteBuffer + ":"
								+ getName());
					} catch (Exception e) {
						Assert.fail();
						e.printStackTrace();
					}
					
					countDownLatch.countDown();

				}

				private String getName() {
					return Thread.currentThread().getName();
				}
			});

		}
		
		countDownLatch.await();
		newFixedThreadPool.shutdownNow();
		

	}

	@Test
	public void testReadForMultiTimes() {
		NativeHeapCacheImpl<Integer> instance = NativeHeapCacheImpl.getInstance(
				100, 900);

		int key = 1;
		byte[] value = UUID.randomUUID().toString().getBytes();
		instance.put(key, value);

		String firstReadStr = new String(instance.get(key));
		String secondReadStr = new String(instance.get(key));

		Assert.assertEquals(firstReadStr, secondReadStr);
	}

	@Test
	public void testReadForMultiTheads() {
		int threadNumber = 200;
		final NativeHeapCacheImpl<Integer> instance = NativeHeapCacheImpl
				.getInstance(100, 1000000);

		int key = 1;
		final StringBuffer originalValue = new StringBuffer(100);
		for (int i = 0; i < 500; i++) {
			originalValue.append(UUID.randomUUID().toString());
		}

		System.out.println(originalValue);
		instance.put(key, originalValue.toString().getBytes());

		ExecutorService newFixedThreadPool = Executors
				.newFixedThreadPool(threadNumber);

		final Set<String> result = new HashSet<String>(threadNumber);
		for (int i = 0; i < threadNumber; i++) {
			newFixedThreadPool.submit(new Callable<String>() {

				@Override
				public String call() throws Exception {
					String value = new String(instance.get(1));
					result.add(value);

					return value;
				}
			});
		}

		Assert.assertTrue(result.size() == 1);
	}

	@Test
	public void testSizeControl() {
		int storeCacheCount = 100;
		NativeHeapCacheImpl<Integer> instance = NativeHeapCacheImpl.getInstance(
				storeCacheCount, 1000);

		for (int i = 0; i < storeCacheCount; i++) {
			byte[] value = UUID.randomUUID().toString().getBytes();
			instance.put(i, value);
		}
		int cacheCount = instance.size();
		long maxSize = instance.getMaxSize();
		long currentSize = instance.getCurrentSize();

		System.out.println(cacheCount);
		System.out.println(maxSize);
		System.out.println(currentSize);

		Assert.assertTrue(cacheCount < 100);

	}

}
