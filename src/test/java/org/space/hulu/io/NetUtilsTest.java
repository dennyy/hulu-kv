package org.space.hulu.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;

import javax.net.SocketFactory;

import org.junit.Test;

/**
 * Class Description
 *
 * @author Denny Ye
 * @since 2012-6-20
 * @version 1.0
 */
public class NetUtilsTest {

	public void createSocket() throws IOException {
		InetSocketAddress address = NetUtils.createSocketAddr("localhost:3333", -1);
		System.out.println(address);
	}
	
	@Test
	public void createSocketFromFactory() throws IOException {
		ServerSocket socket = ServerSocketChannel.open().socket();
		System.out.println("Socket:" + socket.getInetAddress());
	}
}

