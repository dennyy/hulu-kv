package org.space.hulu.storage;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.space.hulu.entry.GroupStatus;
import org.space.hulu.entry.StorageGroup;
import org.space.hulu.entry.StorageInfo;
import org.space.hulu.util.Validation;

/**
 * Primary role in consistency between Storage servers in same group.
 * 
 * The steps we want to take may be looks like as :
 * 1. Register service if it's the first server in group
 * 2. Second one come in. It make the group service disable and content compare,
 *    then it register and this group start to service
 * 3. Other server repeat the step#2, and it must choose the target
 * 
 * Attention:
 * 1. Read operation can work in all steps
 * 2. Does not support write operation
 * 3. Delete operation can be hold on client or retry
 * 
 * @Author Denny Ye
 * @Since 2012-6-22
 */
public class ConsistencyManager {
	private static final Log LOG = LogFactory.getLog(ConsistencyManager.class);
	
	private StorageServer server;
	
	ConsistencyManager(StorageServer server) {
		this.server = server;
	}
	
	/**
	 * To check the consistency in this group
	 * 
	 * @throws IOException 
	 */
	void verify() {
		int groupId = this.server.group.getGroupId();
		StorageGroup group = server.reporter.getStorageInfos(groupId);
		if (group == null || !Validation.isEffectiveList(group.getServers())) {
			LOG.info("Should be serve to group due to the first Storage server");
			
			return;
		} else {
			this.server.reporter.setGroupStatus(groupId, GroupStatus.READ_ONLY);
			
			List<StorageInfo> servers = group.getServers();
			
			StorageInfo target = servers.get(0);
		}
	}
	
}


