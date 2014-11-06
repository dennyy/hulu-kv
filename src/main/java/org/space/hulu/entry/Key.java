package org.space.hulu.entry;

import java.util.Random;

/*
 * Identifier for each chunk. 
 * <br>
 * It follows specified rule
 * 
 * @Author Denny Ye
 * @Since 2012-5-22
 */
public class Key {
	
	private static Random ran = new Random();
	
	
	public static String generateRandomKey() {
		return System.currentTimeMillis() + StorageGroup.CONCAT + ran.nextInt(); 
	}
	
	/**
	 * To judge the rule of actual key
	 * 
	 * @param key comes from client
	 * @return boolean
	 */
	public static boolean isRegularKey(String key) {
		int lastCon = key.lastIndexOf(StorageGroup.CONCAT);
		if (lastCon > 0) {
			String lastPart = key.substring(lastCon + 1);
			if (lastPart.startsWith(StorageGroup.GROUP_PREFIX)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Final key format : ${initKey}-HG${groupId}
	 * 
	 * @param initKey
	 * @return
	 */
	public static String getFinalKey(String initKey, StorageGroup group) {
		StringBuffer sb = new StringBuffer();
		
		sb.append(initKey).append(StorageGroup.CONCAT)
		  .append(StorageGroup.GROUP_PREFIX).append(group.getGroupId());
		
		return sb.toString();
	}
	
	/**
	 * Suits for reading
	 * 
	 * @param key final key for specified data
	 * @return
	 */
	public static int getIdFromKey(String key) {
		int lastCon = key.lastIndexOf(StorageGroup.CONCAT);
		if (lastCon > 0) {
			String lastPart = key.substring(lastCon + StorageGroup.GROUP_PREFIX.length() + 1);
			try {
				return Integer.parseInt(lastPart);
			} catch (NumberFormatException e) {}
		}
		
		throw new IllegalArgumentException("Illegal key : " + key);
	}
	
	/**
	 * @param key final key for specified data
	 * @return
	 */
	public static String getInitKey(String finalKey) {
		int lastCon = finalKey.lastIndexOf(StorageGroup.CONCAT);
		if (lastCon > 0) {
			return finalKey.substring(0, lastCon);
		}
		
		throw new IllegalArgumentException("Illegal key : " + finalKey);
	}

}


