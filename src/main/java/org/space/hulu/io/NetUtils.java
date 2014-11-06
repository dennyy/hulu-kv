package org.space.hulu.io;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import javax.net.SocketFactory;



/**
 * Class Description
 *
 * @author Denny Ye
 * @since 2012-5-25
 * @version 1.0
 */
public class NetUtils {

	/**
	 * Util method to build socket addr from either:
	 */
	public static InetSocketAddress createSocketAddr(String target,
	                                                   int defaultPort) {
		if (target == null) {
			throw new IllegalArgumentException("Target address cannot be null.");
	    }
		
	    int colonIndex = target.indexOf(':');
	    if (colonIndex < 0 && defaultPort == -1) {
	    	throw new RuntimeException("Not a host:port pair: " + target);
	    }
	    
	    String hostname = null;
	    int port = -1;
	    if (!target.contains("/")) {
	    	if (colonIndex == -1) {
	    		hostname = target;
	    	} else {
	    		// must be the old style <host>:<port>
	    		hostname = target.substring(0, colonIndex);
	    		port = Integer.parseInt(target.substring(colonIndex + 1));
	    	}
	    }

	    if (port == -1) {
	    	port = defaultPort;
	    }
	  
	    return new InetSocketAddress(hostname, port);
	}
	
	/**
	   * This is a drop-in replacement for 
	   * {@link Socket#connect(SocketAddress, int)}.
	   * In the case of normal sockets that don't have associated channels, this 
	   * just invokes <code>socket.connect(endpoint, timeout)</code>. If 
	   * <code>socket.getChannel()</code> returns a non-null channel,
	   * connect is implemented using Hadoop's selectors. This is done mainly
	   * to avoid Sun's connect implementation from creating thread-local 
	   * selectors, since Hadoop does not have control on when these are closed
	   * and could end up taking all the available file descriptors.
	   * 
	   * @see java.net.Socket#connect(java.net.SocketAddress, int)
	   * 
	   * @param socket
	   * @param endpoint 
	   * @param timeout - timeout in milliseconds
	   */
	  public static void connect(Socket socket, 
	                             SocketAddress endpoint, 
	                             int timeout) throws IOException {
		  if (socket == null || endpoint == null || timeout < 0) {
			  throw new IllegalArgumentException("Illegal argument for connect()");
		  }
	    
		  SocketChannel ch = socket.getChannel();
	    
		  if (ch == null) {
			  // let the default implementation handle it.
			  socket.connect(endpoint, timeout);
		  } else {
			  connect(ch, endpoint, timeout);
		  }

		  // There is a very rare case allowed by the TCP specification, such that
		  // if we are trying to connect to an endpoint on the local machine,
		  // and we end up choosing an ephemeral port equal to the destination port,
		  // we will actually end up getting connected to ourself (ie any data we
		  // send just comes right back). This is only possible if the target
		  // daemon is down, so we'll treat it like connection refused.
		  if (socket.getLocalPort() == socket.getPort() &&
				  socket.getLocalAddress().equals(socket.getInetAddress())) {
			  socket.close();
			  throw new ConnectException(
					  "Localhost targeted connection resulted in a loopback. " +
			  "No daemon is listening on the target port.");
	    }
	}
	  
	  /**
	   * The contract is similar to {@link SocketChannel#connect(SocketAddress)} 
	   * with a timeout.
	   * 
	   * @see SocketChannel#connect(SocketAddress)
	   * 
	   * @param channel - this should be a {@link SelectableChannel}
	   * @param endpoint
	   * @throws IOException
	   */
	  static void connect(SocketChannel channel, 
	                SocketAddress endpoint, int timeout) throws IOException {
	    
		  boolean blockingOn = channel.isBlocking();
		  if (blockingOn) {
			  channel.configureBlocking(false);
		  }
	    
		  try { 
			  if (channel.connect(endpoint)) {
				  return;
			  }

			  long timeoutLeft = timeout;
			  long endTime = (timeout > 0) ? (System.currentTimeMillis() + timeout): 0;
	      
			  while (true) {
				  // we might have to call finishConnect() more than once
				  // for some channels (with user level protocols)
	        
				  int ret = select((SelectableChannel)channel, 
	                                  SelectionKey.OP_CONNECT, timeoutLeft);
	        
				  if (ret > 0 && channel.finishConnect()) {
					  return;
				  }
	        
				  if (ret == 0 ||
						  (timeout > 0 &&  
								  (timeoutLeft = (endTime - System.currentTimeMillis())) <= 0)) {
					  throw new SocketTimeoutException(
							  timeoutExceptionString(channel, timeout, 
	                                           SelectionKey.OP_CONNECT));
				  }
			  }
	    } catch (IOException e) {
	    	// javadoc for SocketChannel.connect() says channel should be closed.
	    	try {
	    		channel.close();
	    	} catch (IOException ignored) {}
	    	throw e;
	    } finally {
	    	if (blockingOn && channel.isOpen()) {
	    		channel.configureBlocking(true);
	    	}
	    }
	}
	  
	static int select(SelectableChannel channel, int ops, long timeout) 
      				throws IOException {


		SelectionKey key = null;
		SelectorProvider provider = channel.provider();
		Selector selector = provider.openSelector();
		int ret = 0;

		try {
			while (true) {
				long start = (timeout == 0) ? 0 : System.currentTimeMillis();

				key = channel.register(selector, ops);
				ret = selector.select(timeout);

				if (ret != 0) {
					  return ret;
				  }

				 /* Sometimes select() returns 0 much before timeout for 
				 * unknown reasons. So select again if required.
				 */
				 if (timeout > 0) {
					 timeout -= System.currentTimeMillis() - start;
					 if (timeout <= 0) {
						 return 0;
					 }
				 }

				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedIOException("Interruped while waiting for " +
								"IO on channel " + channel +
								". " + timeout + 
								" millis timeout left.");
				}
			}
		 } finally {
			 if (key != null) {
				 key.cancel();
			 }

			 //clear the canceled key.
			 try {
				 selector.selectNow();
			 } catch (IOException e) {
//				 StringUtils.stringifyException(e));
				 // don't put the selector back.
				 return ret; 
			 }

		 }
	}
	  
	private static String timeoutExceptionString(SelectableChannel channel,
              long timeout, int ops) {

		String waitingFor;
		switch(ops) {
			case SelectionKey.OP_READ :
				waitingFor = "read"; break;
			
			case SelectionKey.OP_WRITE :
				waitingFor = "write"; break;      
			
			case SelectionKey.OP_CONNECT :
				waitingFor = "connect"; break;
			
			default :
				waitingFor = "" + ops;  
		}

		return timeout + " millis timeout while " +
			"waiting for channel to be ready for " + 
			waitingFor + ". ch : " + channel;    
	}

	  
	public static SocketFactory getSocketFactory() {
		return SocketFactory.getDefault();
	}
}

