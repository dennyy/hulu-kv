package org.space.hulu.client.group;

import java.util.List;
import java.util.Set;

import org.space.hulu.entry.StorageGroup;

/**
 * Selecting group from list with specified strategy
 *
 * @author Denny Ye
 * @since 2012-6-1
 * @version 1.0
 */
public interface GroupSelectStrategy {
	
	/**
	 * Select on Group to upload
	 * <br>
	 * Maybe return null
	 * 
	 * @param groupList from Zookeeper
	 * @param key data key
	 * @param data 
	 * @param replicaFactor
	 * @param excluded
	 * @return pick out from list
	 */
	public StorageGroup select(List<StorageGroup> groupList, String key, 
			byte[] data, int replicaFactor, Set<StorageGroup> excluded); 
}

