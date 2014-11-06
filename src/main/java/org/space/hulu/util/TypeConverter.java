/**
 * 
 */
package org.space.hulu.util;

import java.nio.ByteBuffer;

/**
 * @author pelu2
 * @date Jun 21, 2012
 */
public final class TypeConverter {

	public static int byteArrayToInt(byte[] b) 
	{
	    int value = 0;
	    for (int i = 0; i < 4; i++) {
	        value = (value << 8) | (b[i] & 0xFF);
	    }
	    return value;
	}

	public static byte[] intToByteArray(int a)
	{
	    byte[] ret = new byte[4];
	    ret[3] = (byte) (a & 0xFF);   
	    ret[2] = (byte) ((a >> 8) & 0xFF);   
	    ret[1] = (byte) ((a >> 16) & 0xFF);   
	    ret[0] = (byte) ((a >> 24) & 0xFF);
	    return ret;
	}
	
	
	public static byte[] shortToByteArray(short s){
		  byte[] ret = new byte[4];
		    ret[1] = (byte) (s  & 0xFF);   
		    ret[0] = (byte) ((s >> 8) & 0xFF);
		    return ret;
		
//		ByteBuffer buffer = ByteBuffer.allocate(2);
//		buffer.putShort(s);
//		buffer.flip();
//		return buffer.array();
	}
	
	public static short byteArrayToShort(byte[] bytes){
		 	int value = (short)0;
		    for (int i = 0; i < 2; i++) {
		        value = (value << 8) | (bytes[i] & 0xFF);
		    }
		    return (short)value;
	}
}
