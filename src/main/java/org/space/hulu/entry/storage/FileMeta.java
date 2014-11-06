package org.space.hulu.entry.storage;

/**
 * Meta-data of local file to communicate with other 
 * Storage server in this group
 * 
 * @Author Denny Ye
 * @Since 2012-7-4
 */
public class FileMeta {

	private short serverNumber;
	
	private String fileName;
	
	private long length;
	
	private String digest;
	
	public FileMeta(short number, String fileName, long length) {
		this.serverNumber = number;
		this.fileName = fileName;
		this.length = length;
	}

	public String getDigest() {
		return digest;
	}

	public void setDigest(String digest) {
		this.digest = digest;
	}

	public String getFileName() {
		return fileName;
	}

	public long getLength() {
		return length;
	}

	public short getServerNumber() {
		return serverNumber;
	}
	
}


