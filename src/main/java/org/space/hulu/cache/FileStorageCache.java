package org.space.hulu.cache;

import java.io.IOException;

import org.space.hulu.cache.info.FileContentCacheInfoReport;
import org.space.hulu.cache.info.FilePointerCacheInfoReport;
import org.space.hulu.entry.storage.FilePointer;
import org.space.hulu.exception.FileContentDeletedException;
import org.space.hulu.exception.FilePointerNoFoundException;

public interface FileStorageCache {

	boolean storePacket(String key, byte[] packet) throws IOException;

	/**
	 * judge if the file pointer exist in cache.
	 * 
	 * @param key
	 * @return
	 */
	boolean isExistFilePointerForKey(String key);

	public void putIntoCache(String key, byte[] packet, FilePointer filePointer);

	/**
	 * 
	 * TODO : delete actual disk storage Delete success in followed cases: <li>
	 * can't find the file pointer according to the key; <li>the
	 * {@link org.space.hulu.entry.storage.FilePointer#isDeleted()} be
	 * {@code false}; <li>delete without any exception.
	 * 
	 * @param key
	 * @return
	 */
	public boolean removePacket(String key);

	/**
	 * @param key
	 * @return
	 * @throws IOException
	 * @throws FilePointerNoFoundException
	 * @throws FileContentDeletedException
	 */
	byte[] retrievePacket(String key) throws IOException, FilePointerNoFoundException, FileContentDeletedException;

	FileContentCacheInfoReport getFileContentCacheInfoReport();

	FilePointerCacheInfoReport getFilePointerCacheInfoReport();
}