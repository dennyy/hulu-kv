package org.space.hulu.client;

import java.io.IOException;

/**
 * General using method : Cluster or Local
 * 
 * @Author Denny Ye
 * @Since 2012-5-24
 */
public interface Connector {

	/**
	 * Uploads data content to Hulu
	 * 
	 * @param key user defined or random
	 * @param data content
	 * @param replicaFactor number
	 * @return actual key
	 * @throws IOException
	 */
	String upload(String key, byte[] data, int replicaFactor) throws IOException;
	
	/**
	 * Delete data and nothing to do
	 * 
	 * @param key
	 * @throws IOException
	 */
	void delete(String key) throws IOException;
	
	/**
	 * Reads data content
	 * 
	 * @param key actual key
	 * @return byte content or exception
	 * @throws IOException
	 */
	byte[] readData(String key) throws IOException;
	
	
	/**
	 * Status of Hulu and Client
	 * 
	 * @return
	 * @throws IOException
	 */
	String getStatus() throws IOException;
	
	/**
	 * Status of specified group and client
	 * 
	 * @param groupId
	 * @return
	 * @throws IOException
	 */
	String getStatus(int groupId) throws IOException;
	
	/**
	 * Release resource related
	 * 
	 * @throws IOException
	 */
	void close() throws IOException;
	
}


