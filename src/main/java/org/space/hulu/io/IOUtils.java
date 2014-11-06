package org.space.hulu.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.apache.commons.logging.Log;

/**
 * Class Description
 *
 * @author Denny Ye
 * @since 2012-6-7
 * @version 1.0
 */
public class IOUtils {

	/**
	 * @param input all input streams
	 * @param log
	 */
	public static void closeInputStream(InputStream input, Log log) {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				log.warn("Cannot close the input stream." + input);
			}
		}
	}
	
	
	/**
	 * @param output all output streams
	 * @param log
	 */
	public static void closeOutputStream(OutputStream output, Log log) {
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
				log.warn("Cannot close the output stream." + output);
			}
		}
	}
	
	public static void closeRandomAccessFile(RandomAccessFile file, Log log) {
		if (file != null) {
			try {
				file.close();
			} catch (IOException e) {
				log.warn("Cannot close the output stream." + file);
			}
		}
	}
	
	public static byte[] int2byte(int res) {
		byte[] targets = new byte[4];

		targets[0] = (byte) (res & 0xff);
		targets[1] = (byte) ((res >> 8) & 0xff);
		targets[2] = (byte) ((res >> 16) & 0xff);
		targets[3] = (byte) (res >>> 24);
		
		return targets; 
	} 
	
	/**
	 * Get key definition from stream
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static String getShortKey(DataInputStream input) throws IOException {
		short len = input.readShort();
		byte[] keyBuf = new byte[len];
		input.read(keyBuf);
		
		return new String(keyBuf);
	}
	
	/**
	 * Get byte collection from stream
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static byte[] getShortData(DataInputStream input) throws IOException {
		short len = input.readShort();
		byte[] dataBuf = new byte[len];
		input.read(dataBuf);
		
		return dataBuf;
	}
	
	/**
	 * Get byte collection from stream
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static byte[] getIntData(DataInputStream input) throws IOException {
		int len = input.readInt();
		byte[] dataBuf = new byte[len];
		input.read(dataBuf);
		
		return dataBuf;
	}
	
	/**
	 * @param input
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public static byte[] getFixedData(DataInputStream input, int len) throws IOException {
		byte[] dataBuf = new byte[len];
		input.read(dataBuf);
		
		return dataBuf;
	}
	
}

