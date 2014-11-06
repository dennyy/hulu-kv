package org.space.hulu.storage;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException;
import org.space.hulu.cache.info.FileContentCacheInfoReport;
import org.space.hulu.cache.info.FilePointerCacheInfoReport;
import org.space.hulu.entry.GroupStatus;
import org.space.hulu.entry.StorageGroup;
import org.space.hulu.entry.StorageInfo;
import org.space.hulu.util.Marks;
import org.space.hulu.util.Validation;
import org.space.hulu.zookeeper.ZkStorageAccessor;

/**
 * Takes responsibility for communication with Zookeeper.
 * It communicate periodically to report Storage server status 
 * for client selection.
 * 
 * @Author Denny Ye
 * @Since 2012-6-3
 */
public class StatusReporter extends Thread {
	private static final Log LOG = LogFactory.getLog(StatusReporter.class);
	
	private StorageServer server;
	private ZkStorageAccessor zkClient;
	
	public StatusReporter(StorageServer server) {
		super("StatusReporter");
		
		this.server = server;
		this.zkClient = new ZkStorageAccessor();
	}
	
	/**
	 * Register this Storage server to Zookeeper
	 * 
	 * @return
	 */
	public boolean register(StorageGroup group, StorageInfo info) {
		try {
			zkClient.register2ZK(group, info);
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	public StorageGroup getStorageInfos(int groupId) {
		try {
			return zkClient.getGroupById(groupId);
		} catch (Exception e) {
			LOG.warn("Failed at Zookeeper with cause : " + e.getMessage());
			return null;
		}
	}
	
	public void setGroupStatus(int groupId, GroupStatus status) {
		//TODO report to Zookeeper
	}
	
	public void run() {
		int interval = server.getConfig().getInt("storage.zk.report.interval", 3000);
		
		LOG.info(getName() + " starting [interval=" + interval + "]");
		
		while (server.isRunning) {
			try {
				Thread.sleep(interval);
				
				
				StorageInfo info = server.current;
				
				StringBuffer appender = new StringBuffer();
				
				FilePointerCacheInfoReport pointerReport = server.fileStorage.getFilePointerCacheInfoReport();
				if (pointerReport != null) {
					appender.append("Pointer:").append(pointerReport).append(Marks.LF);
				}
					
				FileContentCacheInfoReport contentReport = server.fileStorage.getFileContentCacheInfoReport();
				if (contentReport != null) {
					appender.append("Content:").append(contentReport).append(Marks.LF);
				}

				info.setMeta(appender.toString());
				zkClient.updateInfo2ZK(server.group.getGroupId(), info);
			} catch (InterruptedException e) {
				LOG.warn(getName() + " InterruptedException. " + e.getMessage());
			} catch (KeeperException e) {
				LOG.warn(getName() + " access Zookeeper failure. " + e.getMessage());
			} catch (IOException e) {
				LOG.warn(getName() + " connection failure." + e.getMessage());
			}
		}
		
		LOG.info(getName() + " ended");
	}
	
	
}


