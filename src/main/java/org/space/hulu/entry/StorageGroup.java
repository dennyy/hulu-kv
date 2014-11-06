package org.space.hulu.entry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Group for multiple storage server with full replication.
 * 
 * @Author Denny Ye
 * @Since 2012-5-22
 */
public class StorageGroup {
	
	public static final String GROUP_PREFIX = "HG";
	public static final String CONCAT = "-";

	
	private int groupId;
	
	/**
	 * Standard server for replication
	 */
	private short replicaFactor;
	
	/** Servers managed by group */
	private List<StorageInfo> servers = new ArrayList<StorageInfo>();
	
	private String status;
	
	public StorageGroup(int id) {
		this.groupId = id;
	}
	
	public short getReplicaFactor() {
		return replicaFactor;
	}
	
	/**
	 * Group identifier
	 * 
	 * @return
	 */
	public int getGroupId() {
		return groupId;
	}
	
	/**
	 * Group identifier name 
	 * 
	 * @return
	 */
	public String getGroupName() {
		return GROUP_PREFIX + groupId;
	}
	
	public List<StorageInfo> getServers() {
		return servers;
	}
	
	public void setReplicaFactor(short replicaFactor) {
		this.replicaFactor = replicaFactor;
	}

	public void setServers(List<StorageInfo> servers) {
		this.servers = servers;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof StorageGroup)) {
			return false;
		}
		
		StorageGroup group = (StorageGroup) obj;
		return group.groupId == this.groupId;
	}
	
	public int hashCode() {
		return groupId * 31;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("[").append(getGroupName()).append(", factor:")
		  .append(replicaFactor).append(", servers:")
		  .append(servers.size()).append("]");
		
		return sb.toString();
	}
	
	/**
	 * Sorts the group with replica factor
	 *
	 * @author Denny Ye
	 * @since 2012-5-23
	 * @version 1.0
	 */
	public static class GroupComparator implements Comparator<StorageGroup> {

		public int compare(StorageGroup g1, StorageGroup g2) {
			return g1.replicaFactor - g2.replicaFactor;
		}
		
	}

	
}


