package org.space.hulu.entry;

/**
 * @Author Denny Ye
 * @Since 2012-7-4
 */
public enum GroupStatus {

	FULL("full"), READ_ONLY("read"), DISABLE("disable");
	
	String status;
	
	GroupStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return this.status;
	}
	
}


