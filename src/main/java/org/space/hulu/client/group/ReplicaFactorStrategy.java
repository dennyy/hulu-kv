package org.space.hulu.client.group;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.space.hulu.entry.StorageGroup;
import org.space.hulu.util.Validation;

/**
 * Class Description
 *
 * @author Denny Ye
 * @since 2012-6-1
 * @version 1.0
 */
public class ReplicaFactorStrategy implements GroupSelectStrategy {

	@Override
	public StorageGroup select(List<StorageGroup> groupList, String key,
			byte[] data, int replicaFactor, Set<StorageGroup> excluded) {
		if (!Validation.isEffectiveList(groupList)) {
			return null;
		}
		
		Collections.sort(groupList, new StorageGroup.GroupComparator());
		
		int pos = -1;
		for (int i = 0; i < groupList.size(); i++) {
			if (replicaFactor <= groupList.get(i).getReplicaFactor()) {
				pos = i;
				break;
			}
		}
		
		if (pos == -1) { //Set the appropriate factor
			pos = groupList.size() - 1;
		}
		
		return groupList.get(pos);
	}

}

