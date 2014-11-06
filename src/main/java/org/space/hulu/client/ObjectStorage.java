package org.space.hulu.client;

import java.io.Serializable;

/*
 * Here is the external function for taking object storage 
 * to Hulu client. It can be implemented looks like with Redis.
 * <br>
 * This implementation supports regular object and collection.
 * 
 * @Author Denny Ye
 * @Since 2012-5-21
 */
public class ObjectStorage {
	
	/**
	 * Stores instance data to Hulu with default Java 
	 * serialization. 
	 * 
	 * @param instance regular instance
	 * @return key of this instance
	 */
	public static String store(Object instance) {
		if (!(instance instanceof Serializable)) {
			throw new IllegalArgumentException("un-serialized instance");
		}
		
		
		return null;
	}
	
}


