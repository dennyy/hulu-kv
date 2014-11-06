package org.space.hulu.client;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.space.hulu.client.group.GroupSelectStrategy;
import org.space.hulu.client.group.RandomStrategy;
import org.space.hulu.entry.Key;
import org.space.hulu.entry.StorageGroup;
import org.space.hulu.entry.StorageInfo;
import org.space.hulu.io.DataTransferProtocol;
import org.space.hulu.io.IOUtils;
import org.space.hulu.io.NetUtils;
import org.space.hulu.io.Packet;
import org.space.hulu.zookeeper.ZkStorageAccessor;
import org.space.hulu.util.Marks;
import org.space.hulu.util.Validation;

/**
 * It's standard model for Hulu cluster. Using Zookeeper as 
 * server namespace management and uploading data to remote
 * Storage server.
 * 
 * @Author Denny Ye
 * @Since 2012-5-24
 */
public class ClusterConnector implements Connector {
	private static final Log LOG = LogFactory.getLog(ClusterConnector.class);
	
	
	/** Request seqNum for this client */
	private static volatile int seqId;
	
	/** Key is Group name, Value is connection in group */
	private Map<String, GroupConnection> connectionCache = new HashMap<String, GroupConnection>();
	
	private ZkStorageAccessor zkAccessor;
	private GroupSelectStrategy selector;
	
	public ClusterConnector() {
		zkAccessor = new ZkStorageAccessor();
		selector = new RandomStrategy();
	}
	
	/* (non-Javadoc)
	 * @see org.space.hulu.client.Connector#upload(java.lang.String, byte[], int)
	 */
	public String upload(String initKey, byte[] data, int replicaFactor) throws IOException {
		GroupConnection connected = getConnection(initKey, data, replicaFactor);
		
		Packet packet = new Packet(initKey, data);
		connected.write(initKey, packet.getPacket());
		
		return Key.getFinalKey(initKey, connected.group); 
	}
	
	/* (non-Javadoc)
	 * @see org.space.hulu.client.Connector#readData(java.lang.String)
	 */
	public byte[] readData(String finalKey) throws IOException {
		GroupConnection connected = getConnectionFromFinalKey(finalKey);
		byte[] data = connected.read(Key.getInitKey(finalKey));
		return data;
	}

	/* @param finalKey
	 * @see org.space.hulu.client.Connector#delete(java.lang.String)
	 */
	public void delete(String finalKey) throws IOException {
		GroupConnection connected = getConnectionFromFinalKey(finalKey);
		connected.delete(Key.getInitKey(finalKey));
	}
	
	/* (non-Javadoc)
	 * @see org.space.hulu.client.Connector#getStatus()
	 */
	public String getStatus() throws IOException {
		List<StorageGroup> groups = zkAccessor.getAllGroups();
		StringBuffer sb = new StringBuffer();
		
		if (groups != null && groups.size() > 0) {
			sb.append("Hulu Cluster Information").append(Marks.LF)
			  .append("============ Total ==============").append(Marks.LF)
			  .append("Total groups : " + groups.size()).append(Marks.LF)
			  .append("Zookeeper    : " + zkAccessor.getZookeeperStr())
			  .append(Marks.LF)
			  .append("=================================")
			  .append(Marks.LF);
			
			for (StorageGroup group : groups) {
				sb.append(getGroupDescription(group, group.getGroupId()));
				sb.append("=================================");
			}
		} else {
			sb.append("Missing Groups info");
		}
		
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.space.hulu.client.Connector#getStatus(int)
	 */
	public String getStatus(int groupId) throws IOException {
		StorageGroup group = zkAccessor.getGroupById(groupId);
		return getGroupDescription(group, groupId);
	}
	
	private String getGroupDescription(StorageGroup group, int groupId) {
		StringBuffer sb = new StringBuffer();
		if (group != null) {
			sb.append("Group:").append(group).append(Marks.LF);
			
			for (StorageInfo info : group.getServers()) {
				sb.append(Marks.TAB).append("Server:").append(info.getServerName()).append(Marks.LF)
				  .append(Marks.TAB).append("Meta-data:").append(info.getMeta()).append(Marks.LF);
			}
		} else {
			sb.append("Missing status of group#" + groupId + Marks.LF);
		}
		
		return sb.toString();
	}
	
	private StorageGroup getGroupFromFinalKey(String key) throws IOException {
		int groupId = Key.getIdFromKey(key);
		StorageGroup group = zkAccessor.getGroupById(groupId);
		if (group == null || group.getServers() == null 
										|| group.getServers().size() == 0) {
			throw new IOException("Group not found from Zookeeper. group#" + groupId);
		}
		
		return group;
	}
	
	/**
	 * 
	 * 
	 * @param group 
	 * @return
	 * @throws IOException
	 */
	private GroupConnection getConnectionFromFinalKey(String finalKey) throws IOException {
		StorageGroup group = getGroupFromFinalKey(finalKey);
		
		GroupConnection connected = null;
		int limit = 3;
		
		do {
			if (connectionCache.containsKey(group.getGroupName())) {
				LOG.debug("Using cached channel to group#" + group.getGroupName());
				connected = connectionCache.get(group.getGroupName());
			} else {
				GroupConnection newConnection = new GroupConnection(group);
				if (newConnection.isConnected) {
					connected = newConnection;
					connectionCache.put(group.getGroupName(), newConnection);
				}
			}
			
			limit--;
		} while (!(connected != null && connected.isConnected) && limit >= 0);
		
		if (connected == null) {
			throw new IOException("No group choosen.");
		}
		
		return connected;
	}
	
	private GroupConnection getConnection(String key, byte[] data, int replicaFactor) throws IOException {
		Set<StorageGroup> excluded = new HashSet<StorageGroup>();
		GroupConnection connected = null;
		int limit = 3;
		
		do {
			List<StorageGroup> groups = zkAccessor.getAllGroups();
			StorageGroup group = selector.select(groups, key, data, replicaFactor, excluded);
			if (group == null) {
				throw new IOException("No group left");
			}
			
			if (connectionCache.containsKey(group.getGroupName())) {
				LOG.debug("Using cached channel to group#" + group.getGroupName());
				connected = connectionCache.get(group.getGroupName());
			} else {
				GroupConnection newConnection = new GroupConnection(group);
				if (newConnection.isConnected) {
					connected = newConnection;
					connectionCache.put(group.getGroupName(), newConnection);
				} else {
					LOG.info("Group:" + group + " has been moved to exclued set");
					newConnection.releaseConnection();
					excluded.add(group);
				}
			}
			
			limit--;
			
		} while (!(connected != null && connected.isConnected) && limit >= 0);
		
		if (connected == null) {
			throw new IOException("No group choosen. key:'" + key + "'");
		}
		
		if (!connected.isAlive()) {
			connected.start();
		}
		
		return connected;
	}
	
	/**
	 * To manage connection and failure to specified group
	 *
	 * @author Denny Ye
	 * @since 2012-6-4
	 * @version 1.0
	 */
	private class GroupConnection extends Thread {
		
		private StorageGroup group;
		
		private DataOutputStream output;
		private DataInputStream input;
		
		private HashMap<Integer, Call> calls = new HashMap<Integer, Call>();
		private AtomicLong lastActivity = new AtomicLong();
		
		private volatile boolean isConnected;
		
		GroupConnection(StorageGroup group) {
			super("GroupConnection_" + group.getGroupId());
			
			this.group = group;
			
			try {
				initConnection();
				isConnected = true;
			} catch (Exception e) {
				LOG.warn("Failed to connect to " + group, e);
				releaseConnection();
			}
		}
		
		private void initConnection() throws IOException {
			List<StorageInfo> storages = group.getServers();
			
			Validation.effectiveList(storages);
			LOG.info("Connect to " + storages);
			
			StorageInfo header = storages.get(0);
			InetSocketAddress target = NetUtils.createSocketAddr(header.getHost(), header.getPort());
	        Socket s = NetUtils.getSocketFactory().createSocket();
	        
	        int timeoutValue = 10000 * storages.size();
	        NetUtils.connect(s, target, timeoutValue);
	        s.setSoTimeout(timeoutValue);
	        s.setSendBufferSize(64 * 1024);
	        
	        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
	                s.getOutputStream(), 64 * 1024));
	        
	        out.write(DataTransferProtocol.DATA_TRANSFER_VERSION);
	        out.write(DataTransferProtocol.OP_CMD_CONNECT);
	        
	        out.writeShort(storages.size() - 1);
	        
	        for (int i = 1; i < storages.size(); i++) {
				StorageInfo info = storages.get(i);
				out.writeShort(info.getHost().length());
				out.write(info.getHost().getBytes());
				out.writeShort(info.getPort());
			}
	        
	        out.flush();
	        
	        DataInputStream blockReplyStream = new DataInputStream(s.getInputStream());
	        int status = blockReplyStream.readInt();
	        if (status != DataTransferProtocol.OP_STATUS_SUCCESS) {
	        	throw new IOException("Connection failure to Group:" 
	        				+ group + " with wrong status:" + status);
	        }
	        
	        this.output = out;
	        this.input = blockReplyStream;
		}
		
		public void run() {
			if (LOG.isDebugEnabled()) {
				LOG.debug(getName() + ": starting");
			}

			try {
				while (waitForWork()) {//wait here for work - read or close connection
					receiveResponse();
		        }
		    } catch (Throwable t) {
		    	LOG.warn("Unexpected error reading responses on connection " + this, t);
		    }
		      
		    close();
		      
		    if (LOG.isDebugEnabled()) {
		    	LOG.debug(getName() + ": stopped ");
		    }
		}
		
		private synchronized boolean waitForWork() {
//			if (calls.isEmpty() && !isConnected)  {
//				long timeout = maxIdleTime-
//		              (System.currentTimeMillis() - lastActivity.get());
//		        if (timeout > 0) {
//		        	try {
//		        		wait(timeout);
//		        	} catch (InterruptedException e) {}
//		        }
//		    }
//		      
//			if (!calls.isEmpty() && !isConnected) {
//		        return true;
//		    } else if (isConnected) {
//		    	return true;
//		    } else if (calls.isEmpty()) {
//		        return false;
//		    } else { // get stopped but there are still pending requests 
//		    	releaseConnection();
//		        return false;
//		    }
			return true;
		}
		
		private void receiveResponse() {
			if (!isConnected) {
				return;
		    }
			
		    touch();
		      
		    try {
		    	int id = input.readInt();

		        if (LOG.isDebugEnabled()) {
		        	LOG.debug(getName() + " got value #" + id);
		        }

		        Call call = calls.get(id);
		        int state = input.readInt();
		        
				String reconfirmedKey = IOUtils.getShortKey(input);
				if (call.key.equals(reconfirmedKey)) {
					if (state == DataTransferProtocol.OP_STATUS_SUCCESS) {
						if (call.type == CommandType.READ) {
							call.callComplete(IOUtils.getShortData(input));
						} else {
							call.callComplete(true);
						}
					} else if (state == DataTransferProtocol.OP_STATUS_ERROR_CHECKSUM) {
						call.callException(new IOException("Wrong checksum for key:" + call.key));
					} else if (state == DataTransferProtocol.OP_STATUS_ERROR) {
						
						call.callException(new IOException(IOUtils.getShortKey(input)));
					}
				} else {
					String errMsg = "Illegal key from Server with:expect <" + call.key + ">" 
				    						+ ", response <" + reconfirmedKey + ">";
					LOG.warn(errMsg);
					call.callException(new IOException(errMsg));
				}
		        
		        calls.remove(id);
		    } catch (IOException e) {
		    	LOG.warn("Broken response for group : " + group + ", e:" + e.getMessage());
		    }
		}
		
		private void touch() {
			lastActivity.set(System.currentTimeMillis());
		}

		void write(String initKey, byte[] packet) throws IOException {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Write " + packet.length + " bytes with key:'" 
						+ initKey + "' to group:" + group);
			}
			
			Call call = new Call(initKey, CommandType.WRITE);
			calls.put(call.id, call);
			
			synchronized (output) {
				output.writeInt(DataTransferProtocol.OP_CMD_WRITE);
				output.writeInt(call.id);
				
				output.write(packet);
				
				output.flush();
			}
			
			synchronized (call) {
				while (!call.done) {
					try {
						call.wait();
			        } catch (InterruptedException ie) {}
				}

				if (call.error != null) {
					throw call.error;
				}
			}
		}
		
		/**
		 * @param key initKey
		 * @throws IOException
		 */
		byte[] read(String initKey) throws IOException {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Read data with key:" + initKey + " in group " + group.getGroupId());
			}
			
			Call call = new Call(initKey, CommandType.READ);
			calls.put(call.id, call);
			
			synchronized (output) {
				output.writeInt(DataTransferProtocol.OP_CMD_READ);
				output.writeInt(call.id);
				
				output.writeShort(initKey.length());
				output.write(initKey.getBytes());
				
				output.flush();
			}
			
			synchronized (call) {
				while (!call.done) {
					try {
						call.wait();
			        } catch (InterruptedException ie) {}
				}

				if (call.error != null) {
					throw call.error;
				} else {
					byte[] packet = (byte[]) call.value;
					return Packet.resolvePacket(packet.length, packet).getData();
				}
			}
		}
		
		/**
		 * @param key initKey
		 * @throws IOException
		 */
		void delete(String initKey) throws IOException {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Delete key:" + initKey + " in group " + group.getGroupId());
			}
			
			Call call = new Call(initKey, CommandType.DELETE);
			calls.put(call.id, call);
			
			synchronized (output) {
				output.writeInt(DataTransferProtocol.OP_CMD_DELETE);
				output.writeInt(call.id);
				
				output.writeShort(initKey.length());
				output.write(initKey.getBytes());
				
				output.flush();
			}
			
			synchronized (call) {
				while (!call.done) {
					try {
						call.wait();
			        } catch (InterruptedException ie) {}
				}

				if (call.error != null) {
					throw call.error;
				}
			}
		}
		
		public void releaseConnection() {
			IOUtils.closeInputStream(input, LOG);
			IOUtils.closeOutputStream(output, LOG);
			
			calls.clear();
		}
		
	}
	
	/**
	 * Calling info for asynchronous
	 *
	 * @author Denny Ye
	 * @since 2012-6-18
	 * @version 1.0
	 */
	private class Call {
	    int id;            
	    boolean done; 
	    
	    String key;
	    CommandType type;
	    
	    IOException error;
	    Object value;
	    
	    Call(String initKey, CommandType type) {
	    	this.key = initKey;
	    	this.type = type;
	    	
	    	synchronized (Call.class) {
	    		this.id = seqId++;
	        }
	    }
	    
	    synchronized void callComplete(Object value) {
	    	this.done = true;
	    	this.value = value;
	    	
	    	notify();
	    }
	    
	    synchronized void callException(IOException e) {
	    	this.done = true;
	    	this.error = e;
	    	
	    	notify();
	    }
	    
	}
	
	public enum CommandType {
		WRITE, READ, DELETE;
	}

	public void close() {
		for (Map.Entry<String, GroupConnection> entry : connectionCache.entrySet()) {
			entry.getValue().releaseConnection();
		}
	}

	
}
