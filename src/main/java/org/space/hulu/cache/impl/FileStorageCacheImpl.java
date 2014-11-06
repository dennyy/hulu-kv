package org.space.hulu.cache.impl;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.space.hulu.cache.Cache;
import org.space.hulu.cache.FileStorageCache;
import org.space.hulu.cache.SizeLruCache;
import org.space.hulu.cache.common.CacheConfig;
import org.space.hulu.cache.common.CacheConfigImpl;
import org.space.hulu.cache.info.FileContentCacheInfo;
import org.space.hulu.cache.info.FileContentCacheInfoReport;
import org.space.hulu.cache.info.FilePointerCacheInfo;
import org.space.hulu.cache.info.FilePointerCacheInfoReport;
import org.space.hulu.config.CommonConfig;
import org.space.hulu.entry.storage.FilePointer;
import org.space.hulu.exception.FileContentDeletedException;
import org.space.hulu.exception.FilePointerNoFoundException;
import org.space.hulu.storage.FileStorageManager;
import org.space.hulu.util.PerformanceTool;
import org.space.hulu.util.Validation;

public class FileStorageCacheImpl implements FileStorageCache {

	private static final Logger LOGGER = LogManager
			.getLogger(FileStorageCacheImpl.class);
	
	private CacheConfig cacheConfig;
	private FileStorageManager fileStorageManager;
	

	private Cache<String, FilePointer> filePointerCache;
	private FilePointerCacheInfo filePointerCacheInfo;
	private SizeLruCache<String, byte[]> fileContentCache;
	private FileContentCacheInfo fileContentCacheInfo;
 
	public FileStorageCacheImpl(CommonConfig globalConfig) throws IOException {
 		initCache(globalConfig);
 		initFileStorageManager(globalConfig);
	}
 
	private void initCache(CommonConfig config) {
		initCacheConfig(config);
		initFilePointerCache();
		initFileContentCache();
	}
	 
	private void initCacheConfig(CommonConfig config) {
		cacheConfig=CacheConfigImpl.getInstance(config);
	}
	
	private void initFilePointerCache() {
		int initialNumberCapacityForFilePointerCache =cacheConfig.getInitialNumberCapacityForFilePointerCache();
		filePointerCache = ConcurrentCacheImpl
				.getInstance(initialNumberCapacityForFilePointerCache);
		filePointerCacheInfo = FilePointerCacheInfo
				.getInstance(initialNumberCapacityForFilePointerCache);
	}

	private void initFileContentCache() {
		boolean isEnableFileContentCache = cacheConfig.isEnableFileContentCache();
  		long totalSpace = cacheConfig.getMaxSizeForFileContentCache();
		LOGGER.info("[cache] user file content cache ? " + isEnableFileContentCache);
 		
		int initialNumberCapacityForFileContentCache = cacheConfig.getInitialNumberCapacityForFileContentCache();
		fileContentCacheInfo = FileContentCacheInfo.getInstance(isEnableFileContentCache,
				totalSpace, initialNumberCapacityForFileContentCache);
		
		if (isEnableFileContentCache) {
			fileContentCache = NativeHeapCacheImpl.getInstance(
					initialNumberCapacityForFileContentCache, totalSpace);
			LOGGER.info("[cache] heap cache initail info"
					+ fileContentCacheInfo);

		}
	}
 
	private void initFileStorageManager(CommonConfig config) throws IOException {
		fileStorageManager = new FileStorageManager(config);
		fileStorageManager.rubuildIndex(this);
	}
	
	private boolean fileContentCacheEnabled() {
		return fileContentCacheInfo.getIsAlive();
	}

	private boolean fileContentCacheDisabled() {
		return !fileContentCacheInfo.getIsAlive();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.space.hulu.cache.impl.FileStorageCache#storePacket(java.lang.String,
	 * byte[])
	 */
	public boolean storePacket(String key, byte[] packet) throws IOException {
		try {
			FilePointer filePointer = fileStorageManager.storePacket(key,
					packet);
			putIntoCache(key, packet, filePointer);
			return true;

		} catch (Exception e) {
			LOGGER.error("fail to store packet for:" + e.getMessage(), e);
			return false;
		}

	}

	public void putIntoCache(String key, byte[] packet, FilePointer filePointer) {
		filePointerCache.put(key, filePointer);

		// if cache disable.can't put cache
		if (fileContentCacheEnabled()) {
			fileContentCache.put(key, packet);
		}

	}

	/*
	 * (non-Javadoc) *
	 * 
	 * @see
	 * org.space.hulu.cache.impl.FileStorageCache#isExistFilePointerForKey(java.lang.String)
	 */
	public boolean isExistFilePointerForKey(String key) {
		Validation.effectiveStr(key);

		return filePointerCache.get(key) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.space.hulu.cache.impl.FileStorageCache#removePacket(java.lang.String)
	 */
	public boolean removePacket(String key) {
		Validation.effectiveStr(key);

		FilePointer filePointer = filePointerCache.get(key);
		if (filePointer == null || filePointer.isDeleted())
			return true;

		try {
			filePointer.setExist(false);
			
			if (fileContentCacheEnabled()) {
				fileContentCache.remove(key);
			}
		} catch (Exception e) {
			String message = String
					.format("[cache][remove] remove file content cache fail:[%s] for: [%s]",
							key, e.getMessage());
			LOGGER.error(message, e);

			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.space.hulu.cache.impl.FileStorageCache#retrievePacket(java.lang.String
	 * )
	 */
	public byte[] retrievePacket(String key) throws FilePointerNoFoundException, FileContentDeletedException, IOException {
		Validation.effectiveStr(key);

		FilePointer filePointer = filePointerCache.get(key);

		// make sure the file pointer exist and file content exist.
		validateFilePointerExist(filePointer);
		validateFileContentExist(filePointer);

		if (fileContentCacheDisabled())
			return getContentFromDistWithOutUpdateCache(filePointer);

		// Get from cache
		byte[] fileContentInCache = getContentFromCache(key);
		if (fileContentInCache != null)
			return fileContentInCache;

		// Get from local disk and update file content cache
		return getContentFromDistAndUpdateCache(key, filePointer);

	}

	private byte[] getContentFromDistAndUpdateCache(String key,
			FilePointer filePointer) throws IOException {
		byte[] fileContentInCache;
		synchronized (key.intern()) {
			fileContentInCache = fileContentCache.get(key);
			if (fileContentInCache != null)
				return fileContentInCache;

			byte[] fileContent = getContentFromDistWithOutUpdateCache(filePointer);
			fileContentCache.put(key, fileContent);
			return fileContent;
		}
	}

	private byte[] getContentFromCache(String key) {
		PerformanceTool performanceTool = PerformanceTool.getInstance();
		if (LOGGER.isDebugEnabled()) {
			performanceTool = PerformanceTool.getInstance();
			performanceTool.start();
		}

		byte[] fileContentInCache = fileContentCache.get(key);
		if (fileContentInCache != null) {

			if (LOGGER.isDebugEnabled()) {
				String message = String.format(
						"[cache] read from file content cache: key:[%s]", key);
				LOGGER.debug(performanceTool.end(message));
			}

			chachHit(key);
			return fileContentInCache;
		}

		cacheMiss(key);
		return null;
	}

	private void validateFileContentExist(FilePointer pointer)
			throws FileContentDeletedException {
		if (pointer.isDeleted()) {
			String message = String.format(
					"[cache] file content has be deleted, filePointer is [%s]",
					pointer);
			if (LOGGER.isDebugEnabled())
				LOGGER.debug(message);
			throw new FileContentDeletedException(message);
		}
	}

	private void validateFilePointerExist(FilePointer pointer)
			throws FilePointerNoFoundException {
		if (pointer == null) {
			String message = String.format(
					"[cache] file pointer not exist: [%s]", pointer);
			LOGGER.error(message);
			throw new FilePointerNoFoundException(message);
		}

	}

	private void chachHit(String key) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("[cache] cache hit, key:" + key);
		fileContentCacheInfo.increaseCacheHit();
	}

	private void cacheMiss(String key) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("[cache] cache mis, key:" + key);

		fileContentCacheInfo.increaseCacheMiss();
	}

	private byte[] getContentFromDistWithOutUpdateCache(FilePointer pointer)
			throws IOException {
		PerformanceTool performanceTool = PerformanceTool.getInstance();
		if (LOGGER.isDebugEnabled()) {
			performanceTool = PerformanceTool.getInstance();
			performanceTool.start();
		}

		byte[] retrievePacket = fileStorageManager.retrievePacket(pointer);

		if (LOGGER.isDebugEnabled()) {
			String message = performanceTool
					.end("[cache] read from local disk:" + pointer);
			LOGGER.debug(message);
		}

		return retrievePacket;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.space.hulu.cache.impl.FileStorageCache#getFileContentCacheInfoReport
	 * ()
	 */
	public FileContentCacheInfoReport getFileContentCacheInfoReport() {
		if (fileContentCacheEnabled()) {
			fileContentCacheInfo
					.setUsedSpace(fileContentCache.getCurrentSize());
			return fileContentCacheInfo.generateReport();
		}
		return null;
	}

	@Override
	public FilePointerCacheInfoReport getFilePointerCacheInfoReport() {
		filePointerCacheInfo.setCacheNumbers(filePointerCache.size());
		return filePointerCacheInfo.generateReport();
	}

}
