package org.space.hulu.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.space.hulu.SystemParams;
import org.space.hulu.cache.FileStorageCache;
import org.space.hulu.cache.impl.FileStorageCacheImpl;
import org.space.hulu.config.CommonConfig;
import org.space.hulu.entry.StorageGroup;
import org.space.hulu.entry.storage.FilePointer;
import org.space.hulu.io.Packet;

/**
 * Primary functions : 
 * <ul>
 * <li>manage file definition</li>
 * <li>store and retrieve file content</li>
 * <li>file consistency</li>
 * <li>rebuild file index</li>
 * </ul>
 *
 * @author Denny Ye
 * @since 2012-5-18
 * @version 1.0
 */
public class FileStorageManager {
	private static final Log LOG = LogFactory.getLog(FileStorageManager.class);
	
	private static final String DEFAULT_DIR = "/hulu/data";
	private static final String FILE_MARK = "_data_";
	private static final String LOCK_FILE = "lock";
	
	private String pathSeparator = File.separator;
	
	private String filePrefix;
	
	private ExecutorService rebuilderPool = Executors.newFixedThreadPool(10);
	
	private Map<String, RandomAccessFile> readerCache = new HashMap<String, RandomAccessFile>();
	
	private String rootDir;
	
	private FileLock lock;
	
	private int fileLimit;
	
	/**
	 * Index of file name
	 */
	private int fileIndex;
	
	/**
	 * File being written
	 */
	private RandomAccessFile current;
	private String currentFileName;
	
	private int currentPos = 0;
	
	public FileStorageManager(CommonConfig config) throws IOException {
		int groupId = config.getIntOrException("storage.group.id");
		filePrefix = StorageGroup.GROUP_PREFIX + groupId + FILE_MARK;
		
		rootDir = config.get(SystemParams.STORAGE_DATA_DIR, DEFAULT_DIR);
		fileLimit = config.getInt(SystemParams.STORAGE_DATA_FILE_LIMIT, 512 << 20);
		
		if (!rootDir.endsWith(pathSeparator)) {
			rootDir = rootDir + pathSeparator;
		}
		
			
		try {
			File dataDir = new File(rootDir);
			if (!dataDir.exists()) {
				LOG.info("Data dir : " + rootDir + " is absent, create it");
				dataDir.mkdirs();
			}
			
			File lockFile = new File(rootDir, LOCK_FILE);
			if (!lockFile.exists()) {
				lockFile.createNewFile();
			}
			
			FileOutputStream output = new FileOutputStream(lockFile);
			FileChannel channel = output.getChannel();
			lock = channel.tryLock();
			if (lock == null) {
				throw new IOException("Data directory [" + rootDir 
							+ "] has been locked by another process!");
			}
			
			String fileName = getPreparedFileName();
			File localFile = new File(rootDir, fileName);
			if (!localFile.exists()) {
				localFile.createNewFile();
			}
			
			current = new RandomAccessFile(rootDir + fileName, "rw");
			currentFileName = localFile.getAbsolutePath();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw e;
		}
		
	}
	
	/**
	 * Rebuild cache with existed file in local disk
	 * 
	 * @param cache rebuild cache
	 */
	public void rubuildIndex(FileStorageCacheImpl cache) {
		LOG.info("Rebuild index process starting");
		
		long begin = System.currentTimeMillis();
		
		File dataDir = new File(rootDir);
		File[] children = dataDir.listFiles();
		if (children != null && children.length > 0) {
			for (File child : children) {
				String filePath = child.getAbsolutePath();
				
				if (!filePath.contains(filePrefix)) {
					if (!child.getName().equals(LOCK_FILE)) {
						LOG.warn("Unknown file : " + filePath);
					}
				} else {
					try {
						RandomAccessFile reader = new RandomAccessFile(child, "rw");
						readerCache.put(filePath, reader);
						
						Rebuilder rebuilder = new Rebuilder(filePath, reader, cache);
						rebuilderPool.submit(rebuilder);
					} catch (IOException e) {
						LOG.warn("Cannot rebuild file : " + child, e);
					}
				}
				
			}
		} else {
			LOG.info("No data file found in " + rootDir);
		}
		
		rebuilderPool.shutdown();
		
		while (!rebuilderPool.isTerminated()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		
		long cost = System.currentTimeMillis() - begin;
		LOG.info("Rebuild index process ended. " + cache.getFilePointerCacheInfoReport().getCacheNumbers() 
						+ " keys in " + cost + " ms");
	}
	
	public String getFilePath(int len) {
		return rootDir + currentPos;
	}
	
	/**
	 * Start new file with stopping a while.
	 */
	private void createNewFile() {
		
	}
	
	/*
	 * Start and end position
	 */
	private byte[] populateFileHeader() {
		return new byte[0];
	}
	
	/**
	 * Retrieves packet judged by pointer
	 * 
	 * @param pointer
	 * @return
	 * @throws IOException
	 */
	public byte[] retrievePacket(FilePointer pointer) throws IOException {
		if (pointer == null) {
			throw new IllegalArgumentException("pointer");
		}
		
		RandomAccessFile reader = null;
		
		String targetFile = pointer.getFilePath();
		if (targetFile.equals(currentFileName)) {
			reader = current;
		} else {
			synchronized (readerCache) {
				if (readerCache.containsKey(pointer.getFilePath())) {
					reader = readerCache.get(pointer.getFilePath());
				} else {
					RandomAccessFile access = null;
					try {
						access = new RandomAccessFile(targetFile, "rw");
					} catch (FileNotFoundException e) {
						LOG.error("Old file is absent. " + targetFile);
						return new byte[0];
					}
					
					reader = access;
					readerCache.put(targetFile, reader);
				}
			}
		}
		
		reader.seek(pointer.getOffset());
		byte[] packet = new byte[pointer.getLen()];
		reader.read(packet);
		
		return packet;
	}
	
	/**
	 * Stores to local disk
	 * 
	 * @param key chunk key
	 * @param data
	 * @param checksum
	 * @return pointer to offset
	 */
	public FilePointer storePacket(String key, byte[] packet) throws IOException {
		long pos = current.getFilePointer();
		
		current.writeInt(packet.length);
		current.write(packet);
		
		FilePointer pointer = new FilePointer(currentFileName, (int) pos + 4, packet.length);
		return pointer;
	}
	
	/**
	 * File name format : data_${index}
	 * @return
	 */
	private String getPreparedFileName() {
		File dataDir = new File(rootDir);
		String fileName = null;
		File[] children = dataDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(filePrefix);
			}
		});
		
		if (children == null || children.length == 0) {
			fileIndex = 0;
			fileName = filePrefix + fileIndex;
			LOG.info("Data dir is empty, create new file : " + fileName);
			
			return fileName;
		}
		
		Arrays.sort(children, new Comparator<File>() {
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		File lastOne = children[children.length - 1];
		
		String name = lastOne.getName();
		int index = Integer.parseInt(name.substring(name.lastIndexOf("_") + 1));
		if (lastOne.length() >= fileLimit) {
			fileIndex = index + 1;
		}  else {
			fileIndex = index;
		}
		
		fileName = filePrefix + fileIndex;
		return fileName;
	}
	
	/**
	 * Rebuilding index from local file concurrently
	 *
	 * @author Denny Ye
	 * @since 2012-7-4
	 */
	private class Rebuilder extends Thread {
		
		private String filePath;
		private RandomAccessFile reader;
		private FileStorageCache inMemoryCache;
		
		Rebuilder(String filePath, RandomAccessFile reader, FileStorageCache cache) {
			this.filePath = filePath;
			this.reader = reader;
			this.inMemoryCache = cache;
		}
		
		public void run() {
			LOG.info("Rebuilding file : " + filePath);
			
			try {
				int offset = (int) reader.getFilePointer();
				while (reader.length() != offset) {
					int nextLen = reader.readInt();
					byte[] packetBuf = new byte[nextLen];
					reader.read(packetBuf);
					
					Packet packet = Packet.resolvePacket(nextLen, packetBuf);
					FilePointer pointer = new FilePointer(filePath, offset, nextLen + 4);
					inMemoryCache.putIntoCache(packet.getKey(), packetBuf, pointer);
					
					offset = (int) reader.getFilePointer();
				}
				
			} catch (IOException e) {
				LOG.warn("Cannot rebuild index file " + filePath);
			}
		}
		
	}
	
	public void closeStorage() throws IOException {
		for (Map.Entry<String, RandomAccessFile> entry : readerCache.entrySet()) {
			entry.getValue().close();
		}
		
		if (lock != null) {
			lock.release();
		}
	}

}

