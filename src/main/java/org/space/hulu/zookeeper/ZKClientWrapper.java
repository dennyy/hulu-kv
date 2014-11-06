package org.space.hulu.zookeeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.space.hulu.config.CommonConfig;

/**
 * this class is repsonible for communicate with zk server, get or set data.
 * 
 * @author pelu2
 * @date May 31, 2012
 */
public class ZKClientWrapper implements Watcher {
	private static final Logger LOG = Logger.getLogger(ZKClientWrapper.class);
	protected volatile ZooKeeper zookeeper;
	private List<ACL> acl = ZooDefs.Ids.OPEN_ACL_UNSAFE;
	private static final ZKClientWrapper instance = new ZKClientWrapper();
	public static final String PATH_DELIMIT = "/";
	private volatile long sessionId;
	private static final int RETRY_TIMES = 3;
	private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(100);

	// TODO
	private final String connectString;
	private final int sessionTimeout;
	
	// singleton
	public static ZKClientWrapper getInstance() {
		return instance;
	}

	protected <E> E executeZKOperate(ZKOperation<E> operation)
			throws KeeperException, InterruptedException {
		KeeperException exception = null;
        for (int i = 1; i <= RETRY_TIMES; i++) {
            try {
                return operation.execute();
            } catch (KeeperException e) {
            	if ((e instanceof KeeperException.SessionExpiredException)
    					|| (e instanceof KeeperException.ConnectionLossException)) {
            		
            		LOG.info("ZK execute " + operation.operationName() + ", at " + i + " times, ZK server throw exception, ", e);
    				createZookeeper(i);
    				exception = e;
    			}else{
    				exception = e;
    			}
            }
        }
        throw exception;
	}

	private synchronized void createZookeeper(int count) {
		if (zookeeper == null) {
			zookeeper = createZK(sessionId);
//			while (!isAvalable()) {
//				String msg = "Conneting to zookeeper server, waiting for a while ... ...";
//				LOG.info(msg);
//				try {
//					Thread.sleep(2000);
//				} catch (Exception e) {
//					// skip it
//				}
//			}
			LOG.info("init zookeeper success.");
		} else {
			try {
				if (count < RETRY_TIMES) {
					LOG.info("The ZK server status is " + zookeeper.getState() + ", count: " + count + ", retry again.");
					return;
				}
				
				// test zookeeper status
				if (isAvalable()) {
					LOG.info("The ZK server status is avalable, zk state is " + zookeeper.getState());
					return;
				}

				zookeeper.exists("/", false);

			} catch (KeeperException e) {
				if ((e instanceof KeeperException.SessionExpiredException)
						|| (e instanceof KeeperException.ConnectionLossException)) {
					try {
						LOG.info("Create ZK instance once more, zk state is " + zookeeper.getState()+ ", for ", e);

						// close old zookeer instance
						zookeeper.close();

						LOG.info("Close the older zk instance. its state is " + zookeeper.getState());
						zookeeper = createZK(sessionId);
						LOG.info("Create one zk instance. its state is " + zookeeper.getState());

					} catch (InterruptedException e1) {
						LOG.error("thread interrupted.", e1);
						Thread.currentThread().interrupt();
					}
				}
			} catch (InterruptedException e2) {
				LOG.error("thread interrupted.", e2);
				Thread.currentThread().interrupt();
			}
		}
	}

	private void startDelayDeleteThread() {
		new DelayDeleteThread().start();
		LOG.info("delete Thread start success.");
	}

	private ZKClientWrapper() {
		CommonConfig config = CommonConfig.getInstance();
		connectString = config.get(ZookeeperConstants.ZOOKEEPER_CLUSTER, "xx.xx.xx.xx");
		sessionTimeout = config.getInt(ZookeeperConstants.ZOOKEEPER_SESSION_TIMEOUT, 30000);
		LOG.info("Zookeeper config : connectStr=" + connectString + ", sessionTimeout=" + sessionTimeout);
		
		createZookeeper(0);
		startDelayDeleteThread();
	}

	private ZooKeeper createZK(final long sessionId) {
		ZooKeeper zk = null;
		try {
			LOG.info("-------create zookeeper instance starting--------");	
			zk = new ZooKeeper(connectString, sessionTimeout, this);
			LOG.info("-------create zookeeper instance success--------");		
			return zk;
		} catch (IOException e) {
			LOG.error("-------create zookeeper instance fail--------", e);
			return null;
		} finally {
			if (zk != null) {
				this.sessionId = zk.getSessionId();
			}
		}
	}

	private static interface ZKOperation<E> {
		E execute() throws KeeperException, InterruptedException;
		String operationName();
	}


	public String createSequenceNode(final String parentPath, final String path, final byte[] data)
			throws KeeperException, InterruptedException {
		return executeZKOperate(new ZKOperation<String>() {
			@Override
			public String execute() throws KeeperException,
					InterruptedException {
				if(null == zookeeper.exists(parentPath, false)){
					zookeeper.create(parentPath, null, acl, CreateMode.PERSISTENT);
				}
				return zookeeper.create(path, data, acl,
						CreateMode.EPHEMERAL_SEQUENTIAL);
			}

			@Override
			public String operationName() {
				return "createSequenceNode path " + path;
			}

		});
	}

	public String createEphemeralNode(final String path, final byte[] data)
			throws KeeperException, InterruptedException {
		return executeZKOperate(new ZKOperation<String>() {
			@Override
			public String execute() throws KeeperException,
					InterruptedException {
				return zookeeper.create(path, data, acl, CreateMode.EPHEMERAL);
			}

			@Override
			public String operationName() {
				return "createEphemeralNode path " + path;
			}

		});
	}

	public String create(final String path, final byte data[])
			throws KeeperException, InterruptedException {
		return executeZKOperate(new ZKOperation<String>() {
			@Override
			public String execute() throws KeeperException,
					InterruptedException {
				return zookeeper.create(path, data, acl, CreateMode.PERSISTENT);
			}

			@Override
			public String operationName() {
				return "create path " + path;
			}
		});
	}

	public List<String> getChildren(final String path, final boolean watch)
			throws KeeperException, InterruptedException {
		return executeZKOperate(new ZKOperation<List<String>>() {
			@Override
			public List<String> execute() throws KeeperException,
					InterruptedException {
				return zookeeper.getChildren(path, watch);
			}

			@Override
			public String operationName() {
				return "getChildren path: " + path;
			}
		});
	}

	public List<String> getChildren(final String path, final Watcher watch)
			throws KeeperException, InterruptedException {
		return executeZKOperate(new ZKOperation<List<String>>() {
			@Override
			public List<String> execute() throws KeeperException,
					InterruptedException {
				return zookeeper.getChildren(path, watch);
			}
			
			@Override
			public String operationName() {
				return "getChildren path: " + path;
			}
		});
	}

	public long getSessionId() {
		return zookeeper.getSessionId();

	}

	public boolean isAvalable() {
		if (zookeeper.getState() == ZooKeeper.States.CONNECTED) {
			return true;
		} else {
			boolean normal = true;
			try {
				zookeeper.exists("/", false);
			} catch (Exception e) {
				normal = false;
			}
			return normal;
		}
	}

	public Stat exists(final String path, final Watcher watcher)
			throws KeeperException, InterruptedException {
		return executeZKOperate(new ZKOperation<Stat>() {
			@Override
			public Stat execute() throws KeeperException, InterruptedException {
				return zookeeper.exists(path, watcher);
			}

			@Override
			public String operationName() {
				return "exist path: " + path;
			}
		});
	}

	public Stat exists(final String path, final boolean watcher)
			throws KeeperException, InterruptedException {
		return executeZKOperate(new ZKOperation<Stat>() {
			@Override
			public Stat execute() throws KeeperException, InterruptedException {
				return zookeeper.exists(path, watcher);
			}
			
			@Override
			public String operationName() {
				return "exist path: " + path;
			}
		});
	}

	public void delete(final String path, final int version)
			throws InterruptedException, KeeperException {
		executeZKOperate(new ZKOperation<Object>() {
			@Override
			public Object execute() throws KeeperException,
					InterruptedException {
				zookeeper.delete(path, version);
				return null;
			}
			
			@Override
			public String operationName() {
				return "delete path: " + path;
			}
		});
	}

	public Stat setData(final String path, final byte data[], final int version)
			throws KeeperException, InterruptedException {
		return executeZKOperate(new ZKOperation<Stat>() {
			@Override
			public Stat execute() throws KeeperException, InterruptedException {
				if (data != null && data.length > 1024 * 1024) {
					LOG.warn("setData is very large. path=" + path);
				}
				return zookeeper.setData(path, data, version);
			}
			
			@Override
			public String operationName() {
				return "set data path: " + path;
			}
		});
	}

	/**
	 * Ensures that the given path exists with the given data, ACL and flags
	 * 
	 * @param path
	 * @param acl
	 * @param flags
	 */
	public void ensurePathExists(final String path) throws KeeperException,
			InterruptedException {
		Stat state = exists(path, false);
		if (state != null) {
			return;
		}

		assert path.startsWith(PATH_DELIMIT);
		String tmpPath = path;
		if (path.endsWith(PATH_DELIMIT)) {
			tmpPath = path.substring(0, path.length() - 1);

		}
		SimpleStack<String> unCreatedPathStack = new SimpleStack<String>();
		unCreatedPathStack.push(tmpPath);
		int lastSlashPos = tmpPath.lastIndexOf(PATH_DELIMIT);
		while (lastSlashPos != 0) {
			tmpPath = tmpPath.substring(0, lastSlashPos);
			
			state = exists(tmpPath, false);
			if(state != null){
				break;
			}
			
			unCreatedPathStack.push(tmpPath);
			lastSlashPos = tmpPath.lastIndexOf(PATH_DELIMIT);
		}
		while (!unCreatedPathStack.empty()) {
			try {
				create(unCreatedPathStack.pop(), null);
			} catch (KeeperException.NodeExistsException e) {
				// skip the exception. some case other client create the
				// path simultaneously
			}
		}

	}

	public void delayDelete(final String path) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					delete(path, -1);
					if (LOG.isInfoEnabled()) {
						LOG.info("delay delete path success. path=" + path);
					}
				} catch (Throwable e) {
					LOG.error("delete error.", e);
					if (path != null && !(e instanceof NoNodeException)) {
						queue.add(this);
					}
				}
			}

		};
		queue.add(task);
	}

	/**
	 * @param siblePath
	 * @param b
	 * @param stat
	 * @return
	 */
	public byte[] getData(final String siblePath, final boolean watch,
			final Stat stat) throws KeeperException, InterruptedException {
		return executeZKOperate(new ZKOperation<byte[]>() {
			@Override
			public byte[] execute() throws KeeperException,
					InterruptedException {
				return zookeeper.getData(siblePath, watch, stat);
			}
			
			@Override
			public String operationName() {
				return "get data path: " + siblePath;
			}
		});
	}

	public byte[] getData(final String path, final Watcher watcher,
			final Stat stat) throws KeeperException, InterruptedException {
		return executeZKOperate(new ZKOperation<byte[]>() {
			@Override
			public byte[] execute() throws KeeperException,
					InterruptedException {
				return zookeeper.getData(path, watcher, stat);
			}
			
			@Override
			public String operationName() {
				return "get data path: " + path;
			}
		});
	}

	private class DelayDeleteThread extends Thread {
		public DelayDeleteThread() {
			setDaemon(true);
			setName("delayDeleteThread");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			while (true) {
				try {
					Runnable task = queue.take();
					task.run();
				} catch (InterruptedException e) {
					LOG.error("DelayDeleteThread interrupted. exit.");
					break;
				}

			}
		}
	}

	public String getConnectStr() {
		return this.connectString;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
	 */
	@Override
	public void process(WatchedEvent event) {
		LOG.info("default wather event: " + event);
	}
}


