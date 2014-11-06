package org.space.hulu.client.group;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.space.hulu.entry.StorageGroup;

/**
 * Default strategy
 *
 * @author Denny Ye
 * @since 2012-6-1
 * @version 1.0
 */
public class RandomStrategy implements GroupSelectStrategy {

	Random ran = new Random();
	
	@Override
	public StorageGroup select(List<StorageGroup> groupList, String key,
			byte[] data, int replicaFactor, Set<StorageGroup> excluded) {
		if (groupList == null) {
			return null;
		}
		
		if (excluded != null) {
			for (StorageGroup ex : excluded) {
				groupList.remove(ex);
			}
		}
		
		if (groupList.size() == 0) {
			return null;
		}
		
		return groupList.get(ran.nextInt(groupList.size()));
		
	}

}

