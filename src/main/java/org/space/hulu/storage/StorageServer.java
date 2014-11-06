package org.space.hulu.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.space.hulu.SystemParams;
import org.space.hulu.cache.FileStorageCache;
import org.space.hulu.cache.impl.FileStorageCacheImpl;
import org.space.hulu.config.CommonConfig;
import org.space.hulu.entry.GroupStatus;
import org.space.hulu.entry.StorageGroup;
import org.space.hulu.entry.StorageInfo;
import org.space.hulu.io.DataTransferProtocol;
import org.space.hulu.io.IOUtils;
import org.space.hulu.io.NetUtils;
import org.space.hulu.io.Packet;
import org.space.hulu.util.Marks;
import org.space.hulu.util.Validation;

/**
 * Primary role of Hulu for taking responsibility of physical store.
 * 
 * @Author Denny Ye
 * @Since 2012-5-30
 */
public class StorageServer {
	private static final Log LOG = LogFactory.getLog(StorageServer.class);
	
	final static Pattern groupInfoPatt = Pattern.compile("^(\\d+)#(\\d{1})#(\\d{1})$");
	private static final short DEFAULT_PORT = 22222;
	
	private List<ConnectionHolder> holders = new ArrayList<ConnectionHolder>();
	private CommonConfig config = CommonConfig.getInstance();
	
	private DataServer dataServer;
	
	private volatile int holderCount;
	
	/** Storage number in this group */
	private short number;
	
	StorageGroup group;
	StorageInfo current = new StorageInfo();
	
	FileStorageCache fileStorage;
	StatusReporter reporter;
	
	volatile boolean isRunning;
	
	StorageServer() throws IOException {
		init();
	}
	
	private void init() throws IOException {
		//Keeps the Zookeeper status is OK
		reporter = new StatusReporter(this);
		
		String groupInfoStr = config.getOrException(SystemParams.STORAGE_GROUP_INFO);
		
		Matcher m = groupInfoPatt.matcher(groupInfoStr);
		if (m.groupCount() != 3) {
			throw new IllegalArgumentException("Wrong '" + SystemParams.STORAGE_GROUP_INFO 
					+ "' value '" + groupInfoStr + ", Usage:<groupId>#<group factor>#<number in group>");
		}
		
		m.find();
		int groupId = Integer.parseInt(m.group(1));
		short factor = Short.parseShort(m.group(2));
		number = Short.parseShort(m.group(3));
		
		checkUnique(groupId, number, factor);
		
		config.set("storage.group.id", groupId);
		
		current.setNumber(number);
		group = new StorageGroup(groupId);
		group.setReplicaFactor(factor);
		
		String localHost = config.getOrException(SystemParams.STORAGE_GROUP_HOST);
		
		ServerSocket ss = new ServerSocket();
		int retry = 10;
		InetSocketAddress address = null;
		short port = DEFAULT_PORT;
		boolean isBinded = false;
		
		do {
			try {
				address = new InetSocketAddress(localHost, port++);
				ss.bind(address);
				isBinded = true;
				
				break;
			} catch (IOException e) {
				LOG.info("Failed to bind " + port + " with " + e.getMessage() + ", retry...");
			}
			
			retry--;
		} while (retry > 0);
		
		if (isBinded) {
			current.setHost(localHost);
			current.setPort(address.getPort());
			
			LOG.info("Storage server started with " + localHost + Marks.COLON + address.getPort());
		} else {
			throw new IOException("Started failed. Cannot bind to port. (final:" + port + ")");
		}
		 
		fileStorage = new FileStorageCacheImpl(config);
		
		//Content consistency
		ConsistencyManager consistency = new ConsistencyManager(this);
		consistency.verify();
		
		reporter.setGroupStatus(groupId, GroupStatus.FULL);
		
		dataServer = new DataServer(ss, config);
		reporter.register(group, current);
		
	}
	
	/**
	 * Checks whether there have conflict for those parameters
	 * 
	 * @throws IOException violate
	 */
	private void checkUnique(int groupId, short number, short factor) throws IOException {
		if (groupId <= 0 || number <= 0 || factor <= 0) {
			throw new IOException("Wrong '" + SystemParams.STORAGE_GROUP_INFO 
					+ "'. It cannot be zero or negative");
		}
		
		if (number > factor) {
			throw new IOException("Number should not exceed the factor. number:" 
								+ number + ", factor:" + factor);
		}
		
		StorageGroup stored = reporter.getStorageInfos(groupId);
		if (stored != null) {
			List<StorageInfo> servers = stored.getServers();
			
			if (Validation.isEffectiveList(servers)) {
				for (StorageInfo server : servers) {
					if (server.getNumber() == number) {
						throw new IOException("Duplicated number setting. number#" + number 
								+ " is stored in Zookeeper for group#" + groupId);
					}
				}
			}
		}
	}
	
	private void startService() {
		isRunning = true;
		dataServer.start();
		
		reporter.start();
		
		LOG.info(getServerIdentifier() + " has started");
		try {
			dataServer.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public CommonConfig getConfig() {
		return config;
	}
	
	public String getServerIdentifier() {
		return "[" + group.getGroupName() + Marks.POUND_KEY + number + "]";
	}
	
	private void stop() {
		isRunning = false;
		
		for (ConnectionHolder holder : holders) {
			holder.releaseConnection();
		}
	}
	
	/**
	 * Controls the network connection between client and Storage server
	 * 
	 * @author Denny
	 *
	 */
	private class DataServer extends Thread {
		
		private ServerSocket ss; 
		private CommonConfig config;
		
		DataServer(ServerSocket ss, CommonConfig config) {
			this.ss = ss;
			this.config = config;
		}
		
		public void run() {
			while (isRunning) {
				try {
					Socket s = ss.accept();
					
					DataInputStream in = new DataInputStream(
										new BufferedInputStream(s.getInputStream())); 
					DataOutputStream output = new DataOutputStream(
										new BufferedOutputStream(s.getOutputStream()));
					
					if (!(in.readByte() == DataTransferProtocol.DATA_TRANSFER_VERSION)) {
						output.write(DataTransferProtocol.OP_STATUS_ERROR_VERSION);
						output.flush();
					}
					
					s.setTcpNoDelay(true);
					s.setSoTimeout(100000);
					s.setReceiveBufferSize(64 * 1024);
					
					if (!(in.readByte() == DataTransferProtocol.OP_CMD_CONNECT)) {
						output.write(DataTransferProtocol.OP_STATUS_ERROR);
						output.flush();
					}
					
					LOG.info(getServerIdentifier() + "Connection from " + s.getRemoteSocketAddress());
					short downstreamNum = in.readShort();
					
					DataOutputStream mirrorOut = null;
					DataInputStream mirrorIn = null;
					if (downstreamNum > 0) {
						//Continue to transfer data to next Storage server
						short nextStopLen = in.readShort();
						byte[] stopNameBuf = new byte[nextStopLen];
						in.read(stopNameBuf);
						
						short port = in.readShort();
						
						String nextServer = new String(stopNameBuf);
						LOG.info(getServerIdentifier() + "Connect to next Storage server : " 
								+ nextServer + ":" + port);
						
						InetSocketAddress target = NetUtils.createSocketAddr(nextServer, port);
				        Socket nextSocket = NetUtils.getSocketFactory().createSocket();
				        
				        int timeoutValue = 10000 * downstreamNum;
				        NetUtils.connect(nextSocket, target, timeoutValue);
				        nextSocket.setSoTimeout(timeoutValue);
				        nextSocket.setSendBufferSize(32 * 1024);
				        
				        mirrorOut = new DataOutputStream(new BufferedOutputStream(
				        		nextSocket.getOutputStream(), 32 * 1024));
				        
				        mirrorOut.write(DataTransferProtocol.DATA_TRANSFER_VERSION);
				        mirrorOut.write(DataTransferProtocol.OP_CMD_CONNECT);
				        mirrorOut.writeShort(downstreamNum - 1);
				        
				        for (int i = 0; i < downstreamNum - 1; i++) {
				        	short stopLen = in.readShort();
				        	mirrorOut.writeShort(stopLen);
							
							byte[] tempStopNameBuf = new byte[stopLen];
							in.read(tempStopNameBuf);
							mirrorOut.write(tempStopNameBuf);
							mirrorOut.writeShort(in.readShort());
						}
				        
				        mirrorOut.flush();
				        
				        mirrorIn = new DataInputStream(nextSocket.getInputStream());
				        int status = mirrorIn.readInt();
				        if (status != DataTransferProtocol.OP_STATUS_SUCCESS) {
				        	throw new IOException("Connection failure to Group:" 
				        				+ group + " with wrong status:" + status);
				        }
					}
					
					output.writeInt(DataTransferProtocol.OP_STATUS_SUCCESS);
					output.flush();
					
					ConnectionHolder holder = new ConnectionHolder(in, output, mirrorIn, mirrorOut, s);
					holders.add(holder);
					holder.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Receives and retrieve data related about client command
	 * 
	 * @author Denny Ye
	 */
	private class ConnectionHolder extends Thread {

		private DataInputStream in;
		private DataOutputStream output;
		
		private DataInputStream mirrorIn;
		private DataOutputStream mirrorOut;
		
		private Socket socket;
		
		ConnectionHolder(DataInputStream in, DataOutputStream output, 
					DataInputStream mirrorIn, DataOutputStream mirrorOut, Socket s) {
			super("Holder_" + holderCount++);
			
			this.in = in;
			this.output = output;
			
			this.mirrorIn = mirrorIn;
			this.mirrorOut = mirrorOut;
			
			socket = s;
		}
		
		public void run() {
			LOG.info(getServerIdentifier() + getName() + " started");
			
			try {
				while (isRunning) {
					opDataConnection();
				}
			} catch (IOException e) {
				LOG.info(getServerIdentifier() + "Client '" 
								+ socket.getRemoteSocketAddress() + " exit.");
			} 
			
			LOG.info(getServerIdentifier() + getName() + " ended");
		}
		
		private void opDataConnection() throws IOException {
			int cmd = in.readInt();
			int seqId = in.readInt();
			
			switch (cmd) {
			case DataTransferProtocol.OP_CMD_WRITE : 
				byte[] packetBuf = IOUtils.getIntData(in);
				
				if (LOG.isDebugEnabled()) {
					LOG.debug("cmd=write, seqId=" + seqId + ", packetLen=" + packetBuf.length);
				}
				
				if (mirrorOut != null) {
					mirrorOut.writeInt(DataTransferProtocol.OP_CMD_WRITE);
					mirrorOut.writeInt(seqId);
					
					mirrorOut.writeShort(packetBuf.length);
					mirrorOut.write(packetBuf);
					
					mirrorOut.flush();
				}
				
				boolean isSucc = false;
				boolean isExisting = false;
				
				Packet packet = null;
				try {
					packet = Packet.resolvePacket(packetBuf.length, packetBuf);
					
					if (!fileStorage.isExistFilePointerForKey(packet.getKey())) {
						isSucc = fileStorage.storePacket(packet.getKey(), packetBuf);
					} else {
						isExisting = true;
					}
					
				} catch (Exception e) {
					LOG.warn("Wrote failure ", e);
				}
				
				output.writeInt(seqId);
				int state = 0;
				if (mirrorIn != null) {
					//feedback
					mirrorIn.readInt();
					state = mirrorIn.readInt();
				}
					
				boolean isFinalFailure = false;
				if ((state == 0 && isSucc) || 
						(state == DataTransferProtocol.OP_STATUS_SUCCESS && isSucc)) {
					output.writeInt(DataTransferProtocol.OP_STATUS_SUCCESS);
				} else {
					output.writeInt(DataTransferProtocol.OP_STATUS_ERROR);
					isFinalFailure = true;
				}
				
				output.writeShort(packet.getKey().length());
				output.write(packet.getKey().getBytes());
				
				if (isFinalFailure && isExisting) {
					String errMsg = String.format(DataTransferProtocol.MSG_DUPLICATED_KEY, 
											packet.getKey(), group.getGroupId());
					output.writeShort(errMsg.length());
					output.write(errMsg.getBytes());
				} else if (isFinalFailure) {
					String errMsg = String.format(DataTransferProtocol.MSG_WROTE_ERR, 
											packet.getKey(), group.getGroupId());
					
					output.writeShort(errMsg.length());
					output.write(errMsg.getBytes());
				}
				
				output.flush();
				break;
			case DataTransferProtocol.OP_CMD_READ :
				String initKey = IOUtils.getShortKey(in);
				
				output.writeInt(seqId);
				byte[] data = null;
				try {
					data = fileStorage.retrievePacket(initKey);
				} catch (Exception e) {
					LOG.warn("File retrieve failure with key :" + initKey, e);
				}
				
				if (LOG.isDebugEnabled()) {
					LOG.debug("cmd=read, seqId=" + seqId + ", key=" + initKey 
							+ ", data=" + (data != null ? data.length : 0));
				}
				
				if (Validation.isEffectiveData(data)) {
					output.writeInt(DataTransferProtocol.OP_STATUS_SUCCESS);
					output.writeShort(initKey.length());
					output.write(initKey.getBytes());
					
					output.writeShort(data.length);
					output.write(data);
				} else {
					output.writeInt(DataTransferProtocol.OP_STATUS_ERROR);
					output.writeShort(initKey.length());
					output.write(initKey.getBytes());
					
					String errMsg = String.format(DataTransferProtocol.MSG_KEY_ABSENT, 
										initKey, group.getGroupId());
					output.writeShort(errMsg.length());
					output.write(errMsg.getBytes());
				}
				
				output.flush();
				break;
			case DataTransferProtocol.OP_CMD_DELETE :
				String initKey2 = IOUtils.getShortKey(in);
				
				boolean isRemoved = fileStorage.removePacket(initKey2);
				output.writeInt(seqId);
				if (isRemoved) {
					output.writeInt(DataTransferProtocol.OP_STATUS_SUCCESS);
				} else {
					output.writeInt(DataTransferProtocol.OP_STATUS_ERROR);
				}
				
				output.writeShort(initKey2.length());
				output.write(initKey2.getBytes());
				
				if (!isRemoved) {
					String errMsg = String.format(DataTransferProtocol.MSG_CANNOT_DELETE, 
											initKey2, group.getGroupId());
					output.writeShort(errMsg.length());
					output.write(errMsg.getBytes());
				}
				
				output.flush();
				break;
			}
			
		}
		
		public void releaseConnection() {
			IOUtils.closeInputStream(in, LOG);
			IOUtils.closeInputStream(mirrorIn, LOG);
			
			IOUtils.closeOutputStream(output, LOG);
			IOUtils.closeOutputStream(mirrorOut, LOG);
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			StorageServer server = new StorageServer();
			
			server.startService();
		} catch (IOException e) {
			LOG.error("Storage server failure", e);
		}

	}

}
