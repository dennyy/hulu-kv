package org.space.hulu.util;

import java.util.zip.Checksum;

/**
 * Data validation
 *
 * @author Denny Ye
 * @since 2012-5-23
 * @version 1.0
 */
public class ChecksumUtils {

	/** CRC32 */
	private static final Checksum sum = new PureJavaCRC32();
	private static final int CRC32_BYTES = 4;
	
	/**
	 * Obtains the CRC32 checksum from specified data
	 * 
	 * @param data comes from user
	 * @return 4 bytes checksum
	 */
	public static byte[] getChecksum(byte[] data) {
		Validation.effectiveData(data);
		
		sum.update(data, 0, data.length);
		int tempChecksum = (int)sum.getValue();
		
		byte[] crcResult = new byte[CRC32_BYTES];
		int2byte(tempChecksum, crcResult);
		sum.reset();
		
		return crcResult;
	}
	
	/**
	 * Verify the data and checksum.
	 * 
	 * @param data actual data
	 * @param checksum expect checksum
	 * @return
	 */
	public static boolean verifyChunksum(byte[] data, byte[] checksum) {
		Validation.effectiveData(data);
		Validation.effectiveData(checksum);
		
		if (checksum.length != CRC32_BYTES) {
			throw new IllegalArgumentException("wrong CRC checksum : len = " + checksum.length);
		}
		
		sum.update(data, 0, data.length);
		long current = sum.getValue();
		sum.reset();
		
		long expect = checksum2long(checksum);
		
		return current == expect;
	}
	
	/*
	 * Transferring integer to byte array
	 * 
	 * @param integer
	 * @param bytes
	 */
	private static void int2byte(int integer, byte[] bytes) {
		bytes[0] = (byte)((integer >>> 24) & 0xFF);
	    bytes[1] = (byte)((integer >>> 16) & 0xFF);
	    bytes[2] = (byte)((integer >>>  8) & 0xFF);
	    bytes[3] = (byte)((integer >>>  0) & 0xFF);
	}
	
	/*
	 * Convert a checksum byte array to a long 
	 */
	private static long checksum2long(byte[] checksum) {
		long crc = 0L;
	    for (int i = 0; i < checksum.length; i++) {
	    	crc |= (0xffL & (long) checksum[i]) << ((checksum.length - i - 1) * 8);
	    }
	    
	    return crc;
	}
	
    
}

