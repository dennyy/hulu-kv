package org.space.hulu.entry;

import org.space.hulu.util.Marks;


/**
 * Meta-data and server status comes from concrete
 * storage server for monitoring and judgement.
 * 
 * TODO suggest use protobuf or json which is easy to serialize or deserialize.
 *
 * @author Denny Ye
 * @since 2012-5-23
 * @version 1.0
 */
public class StorageInfo {
	
	/**/
	private static final long serialVersionUID = 1242464641634774L;

	private String host;
	
	private int port;
	
	/** Number in this group (Start from 1)*/
	private short number;
	
	/** Server meta-data for monitoring */
	private String meta;
	
	public StorageInfo() {
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setHost(String host){
		this.host = host;
	}
	
	public String getHost() {
		return this.host;
	}
	
	public short getNumber() {
		return number;
	}

	public void setNumber(short number) {
		this.number = number;
	}
	
	public String getServerName() {
		return host + Marks.COLON + port + Marks.POUND_KEY + number;
	}
	
	public String getMeta() {
		return meta;
	}
	
	public void setMeta(String meta) {
		this.meta = meta;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("[").append(host).append(Marks.COLON).append(port)
		  .append(Marks.POUND_KEY).append(number).append("]");
		
		return sb.toString();
	}

}

