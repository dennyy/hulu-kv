package org.space.hulu;

/**
 * Here are parameter key definition for providing user in Hulu. <br>
 * It's important with more details configuration. Please refers to parameter
 * name and comment.
 * 
 * @Author Denny Ye
 * @Since 2012-5-24
 */
public class SystemParams {

	/* ================================================== */
	/* ================= Common ========================= */
	/* ================================================== */
	/**
	 * Zookeeper configuration. Splits by comma with multiple instance. (String)
	 */
	public static final String ZOOKEEPER_ADDR = "zookeeper.address";

	/**
	 * Storage top directory in Zookeeper tree-like structure (String)
	 */
	public static final String ZOOKEEPER_TOP_DIR = "zookeeper.top.dir";

	/* ================================================== */
	/* ================= Client ========================= */
	/* ================================================== */

	/**
	 * Throw exception even if missing Zookeeper address (Boolean)
	 */
	public static final String EXCEPTION_MISSING_ZK = "client.exception.missing.zookeeper";

	/**
	 * If user want to dump data to local disk after failed to connect
	 * Zookeeper. It should make local directory usable. (String)
	 */
	public static final String EXCEPTION_USE_LOCAL_DIR = "client.exception.local.dir";

	/* ================================================== */
	/* ============== Storage Server ==================== */
	/* ================================================== */

	/**
	 * Local data directory in each Storage server
	 */
	public static final String STORAGE_DATA_DIR = "storage.data.dir";

	/**
	 * Limitation of each local file stored in Storage server
	 */
	public static final String STORAGE_DATA_FILE_LIMIT = "storage.data.file.limit";
	
	/**
	 * Group identifier from configuration
	 */
	public static final String STORAGE_GROUP_INFO = "storage.group.info";
	
	/** 
	 * Host of Storage server
	 */
	public static final String STORAGE_GROUP_HOST = "storage.group.host";
	
	/**
	 * Limits the server workload in Storage network
	 */
	public static final String MAX_UPLOAD_CONNECT = "storage.maximum.upload.connection";

	
	public static class Cache {

		public static class FilePointerCache {

			private static final String STORAGE_CACHE_FILE_POINTER = "storage.cache.filePointer";

			/**
			 * avoid resize
			 */
			public static final String INITIAL_CACHE_NUMBER_CAPACITY = STORAGE_CACHE_FILE_POINTER + ".initialCacheNumberCapacity";
  
		}

		public static class FileContentCache {

			private static final String STORAGE_CACHE_FILE_CONTENT = "storage.cache.fileContent";

			public static final String ISALIVE = STORAGE_CACHE_FILE_CONTENT
					+ ".enable";

			public static final String INITIAL_CACHE_NUMBER_CAPACITY = STORAGE_CACHE_FILE_CONTENT
					+ ".initialCacheNumberCapacity";

			/**
			 * Allowed total size for all cache object:UNIT: byte
			 */
			public static final String MAX_CACHE_SIZE = STORAGE_CACHE_FILE_CONTENT
					+ ".totalSize";

		}

	}

}
