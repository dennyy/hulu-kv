package org.space.hulu.io;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Class Description
 *
 * @author Denny Ye
 * @since 2012-5-25
 * @version 1.0
 */
public class PacketTest {

	@Test
	public void createAndResolvePacket() {
		Packet packet = new Packet("myKey", "firstOne".getBytes());
		byte[] packetData = packet.getPacket();
		
		byte[] receive = new byte[packetData.length - 4];
		System.arraycopy(packetData, 4, receive, 0, receive.length);
		
		Packet rebuild = Packet.resolvePacket(receive.length, receive);
		Assert.assertEquals("abc", rebuild.getKey());
		Assert.assertEquals("deg", new String(rebuild.getData()));
	}
	
	@Test
	public void exceptionCase() {
		try {
			String key = null;
			new Packet(key, "".getBytes());
		} catch (IllegalArgumentException e) {
		}
	}
}

