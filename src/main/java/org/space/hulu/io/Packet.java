package org.space.hulu.io;

import java.nio.ByteBuffer;

import org.space.hulu.util.ChecksumUtils;
import org.space.hulu.util.Validation;

/**
 * Packet is the data transferring unit between client 
 * and storage server.
 * 
 * <br>
 * The primary function of this class is take responsibility for populating
 * and resolving standard packet format.
 * 
 *
 * @author Denny Ye
 * @since 2012-5-23
 * @version 1.0
 */
public class Packet {

	private String key;
	private byte[] data;
	
	/** lazy */
	private byte[] checksum;
	
	public Packet(String key, byte[] data) {
		Validation.effectiveStr(key);
		Validation.effectiveData(data);
		
		this.key = key;
		this.data = data;
		
		checksum = ChecksumUtils.getChecksum(data);
	}
	
	/**
	 * Packet structure:
	 * <br> packet length : 4 bytes
	 * <br> hold		  : 4 bytes
	 * <br> key length    : 2 bytes
	 * <br> key    		  : n bytes
	 * <br> checksum	  : 4 bytes
	 * <br> data		  : m bytes
	 * 
	 * @return
	 */
	public byte[] getPacket() {
		ByteBuffer buf = ByteBuffer.allocate(4 + 4 + 2 + key.length() + 4 + data.length);
		
		buf.putInt(4 + 2 + key.length() + 4 + data.length);
		
		byte[] hold = new byte[4];
		buf.put(hold);
		buf.putShort((short) key.length());
		buf.put(key.getBytes());
		buf.put(checksum);
		buf.put(data);
		
		return buf.array();
	}
	
	/**
	 * Obtains Packet from prepared bytes
	 * 
	 * @param packetBytes : keylen + key + checksum + data
	 * @return 
	 * @throws IllegalArgumentException broken packet
	 */
	public static Packet resolvePacket(int packetLen, byte[] packetBytes) {
		Validation.effectiveData(packetBytes);
		
		ByteBuffer curPacket = ByteBuffer.wrap(packetBytes);
		
		if (packetLen == packetBytes.length) {
			curPacket.getInt();
			
			short keyLen = curPacket.getShort();
			byte[] keyData = new byte[keyLen];
			
			curPacket.get(keyData);
			String key = new String(keyData);
			
			byte[] checksum = new byte[4];
			curPacket.get(checksum);
			
			byte[] data = new byte[packetLen - 4 - 2 - 4 - keyLen];
			curPacket.get(data);
			
			Packet packet = new Packet(key, data);
			packet.setChecksum(checksum);
			
			return packet;
		}
		
		throw new IllegalArgumentException("Broken packet : expect=" + packetLen 
											+ ", actual=" + packetBytes.length);
	}
	
	public String getKey() {
		return key;
	}

	public byte[] getData() {
		return data;
	}

	public byte[] getChecksum() {
		return checksum;
	}

	public void setChecksum(byte[] checksum) {
		this.checksum = checksum;
	}
	
	public String toString() {
		return "[" + key + ", dataLen:" + data.length + "]";
	}
	
}

