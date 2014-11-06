package org.space.hulu.io;

import java.net.InetSocketAddress;

/**
 * High-level communication between Storage servers
 * <p>
 * It may be used at content consistency and monitoring
 * 
 * @Author Denny Ye
 * @Since 2012-6-22
 */
public class RPC {

	/**
	 * Client interface to obtains specified remote service
	 * 
	 * @param <T> Remote service interface
	 * @param clazz concrete type
	 * @param remote
	 * @return
	 */
	public static <T> T getProxy(Class<T> clazz, InetSocketAddress remote) {
		return null;
	}
	
	
	
}


