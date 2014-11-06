package org.space.hulu.entry.storage;

/**
 * Class Description
 * 
 * @author Denny Ye
 * @since 2012-5-18
 * @version 1.0
 */
public class FilePointer {

	private String filePath;

	private int offset;
	private int len;
	private volatile boolean exist = true;

	public boolean isExist() {
		return exist;
	}

	public boolean isDeleted() {
		return !exist;
	}

	public void setExist(boolean exist) {
		this.exist = exist;
	}

	public FilePointer(String path, int offset, int len) {
		this.filePath = path;
		this.offset = offset;
		this.len = len;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLen() {
		return len;
	}

	public void setLen(int len) {
		this.len = len;
	}

	@Override
	public String toString() {
		return "FilePointer [filePath=" + filePath + ", exist=" + exist
				+ ", offset=" + offset + ", len=" + len + "]";
	}
	
	

}
