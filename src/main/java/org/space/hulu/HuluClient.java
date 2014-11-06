package org.space.hulu;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.space.hulu.client.ClusterConnector;
import org.space.hulu.client.Connector;
import org.space.hulu.config.CommonConfig;
import org.space.hulu.entry.Key;
import org.space.hulu.util.Validation;


/**
 * Here is the primary user interface of Hulu. It follows the standard operation
 * with other K/V storages.
 * <br>
 * <b>Reference</b><br>
 * It relates the support from Zookeeper cluster. Meanwhile, it would like to exit 
 * quickly when there couldn't connect to any Zookeeper cluster.
 * <br>
 * <b>Synchronization</b>
 * This interface support the synchronous uploading model. Applcation may be impacted 
 * by the network or internal business. User can choose the asynchronous client 
 * <{@link AsynHuluClient} with appropriate data cache.
 * 
 * 
 * @author Denny Ye
 * @since 2012-5-18
 * @version 1.0
 */
public class HuluClient {
	private static final Log LOG = LogFactory.getLog(HuluClient.class);
	
	private CommonConfig config = CommonConfig.getInstance();
	
	private Connector connector;
	
	public HuluClient() {
		initAndConnect();
	}
	
	private void initAndConnect() {
		connector = new ClusterConnector();
	}

	/**
	 * Writes data to Hulu with initiative key (it doesn't the 
	 * actual key to retrieve data).
	 * 
	 * @param key initiative user defined or system default
	 * @param data byte array
	 * @param replicat factor replication
	 * @return actual key for data
	 * @throws IOException
	 */
	public String put(String key, byte[] data, int replicaFactor) throws IOException {
		Validation.effectiveStr(key);
		Validation.effectiveData(data);
		
		if (data.length > Validation.Constants.DATA_SIZE_LIMIT) {
			throw new IllegalArgumentException("Large data. len:" + data.length 
						+ ", limit:" + Validation.Constants.DATA_SIZE_LIMIT);
		}
		
		if (key.length() >= Validation.Constants.KEY_SIZE_LIMIT) {
			throw new IllegalArgumentException("Too large key. limit:" 
						+ Validation.Constants.KEY_SIZE_LIMIT);
		}
		
		if (replicaFactor <= 0) {
			replicaFactor = Validation.Constants.DEFAULT_REPLICA;
		}
		
		String actualKey = connector.upload(key, data, replicaFactor);
		
		return actualKey;
	}
	
	/**
	 * Write data to Hulu cluster without key
	 * 
	 * @param data effective byte array
	 * @return actual key to retrieve data
	 * @throws IOException network failure
	 */
	public String put(byte[] data) throws IOException {
		return put(Key.generateRandomKey(), data, Validation.Constants.DEFAULT_REPLICA);
	}
	
	/**
	 * Write data to Hulu cluster without key to appropriate replica factor
	 * 
	 * @param data effective byte array
	 * @param replica factor
	 * @return actual key to retrieve data
	 * @throws IOException network failure
	 */
	public String put(byte[] data, int replicaFactor) throws IOException {
		return put(Key.generateRandomKey(), data, replicaFactor);
	}
	
	/**
	 * Deletes specified key
	 * <br>
	 * Doesn't return any status even if there are 
	 * some cases : Delete successfully;Failed;Missing key
	 * 
	 * @param key
	 * @throws IOException
	 */
	public void delete(String key) throws IOException {
		Validation.effectiveStr(key);
		
		if (Key.isRegularKey(key)) {
			connector.delete(key);
		}
	}

	/**
	 * Reads data from Hulu with actual key(returned from write command)
	 * 
	 * @param key
	 * @return
	 */
	public byte[] get(String key) throws IOException {
		Validation.effectiveStr(key);
		
		if (Key.isRegularKey(key)) {
			return connector.readData(key);
		}
		
		LOG.debug("Illegal key format:" + key);
		return null;
	}

	/**
	 * Status of Hulu cluster.
	 * <br>
	 * It may have those parts:
	 * <li>Group and Storager server number
	 * <li>Meta-data of each Storage server
	 * <li>Current connections to Hulu client
	 * 
	 * @return
	 */
	public String getStatus() throws IOException {
		return connector.getStatus();
	}
	
	/**
	 * Status of specified group
	 * 
	 * @param groupId
	 * @return
	 * @throws IOException
	 */
	public String getStatus(int groupId) throws IOException {
		return connector.getStatus(groupId);
	}
	
	/**
	 * Obtains group information of specified key
	 * 
	 * @param finalKey
	 * @return
	 * @throws IOException
	 */
	public String getLocation(String finalKey) throws IOException {
		Validation.effectiveStr(finalKey);
		
		if (Key.isRegularKey(finalKey)) {
			int groupId = Key.getIdFromKey(finalKey);
			return getStatus(groupId);
		}
		
		return null;
	}
	
	/**
	 * Close all connections outward
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (connector != null) {
			connector.close();
		}
	}
	
	
	public CommonConfig getConfig() {
		return config;
	}

}

