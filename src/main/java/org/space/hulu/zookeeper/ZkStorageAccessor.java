/**
 * 
 */
package org.space.hulu.zookeeper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.space.hulu.entry.StorageGroup;
import org.space.hulu.entry.StorageInfo;
import org.space.hulu.util.TypeConverter;

/**
 * @author pelu2
 * @date Jun 11, 2012
 */
public class ZkStorageAccessor {
	
	private static final Logger LOG = Logger.getLogger(ZkStorageAccessor.class);
	
	private static final String rootPath = "/hulu";
	
	private ZKClientWrapper zkClient = ZKClientWrapper.getInstance();
	
	public ZkStorageAccessor(){
		if(!isValid()){
			throw new RuntimeException("check zk config failed.");
		}
	}
	
	private boolean isValid() {
		try {
			Stat stat = zkClient.exists(rootPath, false);

			if (null == stat) {
				
				try {
					zkClient.create(rootPath, rootPath.getBytes(ENCODE));
				} catch (KeeperException.NodeExistsException nee) {
					// at the same time, there is another server create this node.
				}
				
				return true;
			} else {
				byte[] data = zkClient.getData(rootPath, false, null);
				String path = new String(data, ENCODE);

				if (path.equals(rootPath)) {
					return true;
				} else {
					LOG.warn("Conflict '/hulu' path in Zookeeper. It may be created by unknown service");
					return false;
				}
			}

		} catch (Exception e) {
			LOG.warn("Checked failed in Zookeeper.", e);
			return false;
		}

	}
	
	public List<StorageGroup> getAllGroups(){
		List<StorageGroup> list = new ArrayList<StorageGroup>();
		
		try{
			List<String> pathLst = zkClient.getChildren(rootPath, false);
			for(String groupPath : pathLst){
				
				try{
					int groupId = Integer.parseInt(groupPath);
					StorageGroup group = getGroupById(groupId);
					
					list.add(group);
				}catch(NumberFormatException nfe){
					// continue, ignore this exception, search other group.
				}
			}
		}catch(Exception e){
			LOG.info("", e);
		}
		
		return list;
	}
	
	public StorageGroup getGroupById(int groupId) {
		StorageGroup group = new StorageGroup(groupId);
		group.setReplicaFactor((short) 1);
		
		List<StorageInfo> servers = new ArrayList<StorageInfo>();
		
		String directry = rootPath + "/" + groupId;
		
		short factor;
		
		try {
			
			// get factor from zk and set to StorageGroup.
			byte[] factorBytes = zkClient.getData(directry, false, null);
			factor = TypeConverter.byteArrayToShort(factorBytes);
			group.setReplicaFactor(factor);
			
			List<String> paths = zkClient.getChildren(directry, false);
			
			for(String path : paths){
				path = directry + "/" + path;
				byte[] data = zkClient.getData(path, null, null);
				
				StorageInfo info = deSerialized(data);
				servers.add(info);
			}
		} catch (Exception e) {
			
			LOG.error("", e);
			return null;
		} 
		
		group.setServers(servers);
		
		return group;
	}
	
	public void updateInfo2ZK(int groupId, StorageInfo info) throws KeeperException, InterruptedException, IOException{
		String server_name = info.getServerName();
		
		
		String path = rootPath + "/" + groupId + "/" + server_name;
		byte[] infoBytes = serialize(info);
		
		zkClient.setData(path, infoBytes, -1);
	}
	
	public void register2ZK(StorageGroup group, StorageInfo info){
		String server_name = info.getServerName();
		
		try {
			String directory = rootPath + "/" + group.getGroupId();
			short factor = group.getReplicaFactor();
			
			Stat stat = zkClient.exists(directory, false);
			if(null == stat){
				try{
					zkClient.create(directory, TypeConverter.shortToByteArray(factor));
				}catch(KeeperException.NodeExistsException nee){
					// if throw NodeExistsException, notes: at the same time, there is another server
					// create this node. so igore this exception.
				}
			}
			
			String path = directory + "/" + server_name;
			byte[] data = serialize(info);
			zkClient.createEphemeralNode(path, data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getZookeeperStr() {
		return zkClient.getConnectStr();
	}
	
	private byte[] serialize(StorageInfo info) throws UnsupportedEncodingException{
		JSONObject json = JSONObject.fromObject(info);
		
		String content = (!json.isNullObject() && !json.isEmpty()) ? json.toString() : "";
		
		return content.getBytes(ENCODE);
	}
	
	private StorageInfo deSerialized(byte[] data) throws UnsupportedEncodingException{
		String content = new String(data, ENCODE);
		
		JSONObject json = JSONObject.fromObject(content);
		StorageInfo info = (StorageInfo) JSONObject.toBean(json, StorageInfo.class);
		
		return info;
	}
	
    public static final String ENCODE = "UTF8";

}
