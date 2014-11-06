package org.space.hulu.io;

/**
 * Interaction between Client and Storage server
 *
 * @author Denny Ye
 * @since 2012-5-25
 * @version 1.0
 */
public class DataTransferProtocol {

	/** Header to verify request protocol */
	public static final int DATA_TRANSFER_VERSION = 1;
	
	/** Command */
	public static final byte OP_CMD_CONNECT = (byte) 7;
	public static final byte OP_CMD_WRITE = (byte) 11;
	public static final byte OP_CMD_READ = (byte) 12;
	public static final byte OP_CMD_DELETE = (byte) 15;

	/** Response status from Storage server */
	public static final int OP_STATUS_SUCCESS = 0;  
	public static final int OP_STATUS_ERROR = 1;  
	public static final int OP_STATUS_ERROR_VERSION = 2;  
	public static final int OP_STATUS_ERROR_CHECKSUM = 3;  
	
	/** Error message from Storage server */
	public static final String MSG_DUPLICATED_KEY = "duplicated key [%s] in group [%d]"; 
	public static final String MSG_WROTE_ERR = "Cannot write key [%s] in group [%d]"; 
	public static final String MSG_KEY_ABSENT = "Absent key [%s] in group [%d]"; 
	public static final String MSG_CANNOT_DELETE = "Cannot delete key [%s] in group [%d]"; 
	
	
}

