package org.space.hulu.util;

import java.io.File;
import java.util.List;

/**
 * Utility for parameter validation
 *
 * @Author Denny Ye
 * @Since 2012-5-22
 */
public class Validation {

	/**
	 * Checks for effective list with exception
	 *
	 * @throws IllegalArgumentException
	 *             blank
	 */
	public static <T> void effectiveList(List<T> list) {
		if (!isEffectiveList(list)) {
			throw new IllegalArgumentException("list");
		}
	}

	/**
	 * Checks for effective list
	 *
	 * @return effective
	 * @throws IllegalArgumentException
	 *             blank
	 */
	public static <T> boolean isEffectiveList(List<T> list) {
		return (list != null && list.size() > 0);
	}

	/**
	 * Checks for exist file with exception
	 * @param file
	 * @throws IllegalArgumentException
	 */
	public static void existedFile(File file){
		boolean existedFile = isExistedFile(file);
		if (!existedFile)
			throw new IllegalArgumentException("file path no existed:"+file.getPath());
 	}

	public static boolean isExistedFile(File file){
 		return file!=null&&file.exists();
 	}

	/**
	 * Checks for exist file with exception
	 * @param file
	 * @throws IllegalArgumentException:
	 * <li>file path be null or empty
	 * <li>file path not found.
	 */
	public static void existedFilePath(String filePath){
		effectiveStr(filePath);
		existedFile(new File(filePath));
	}

	/**
	 * @param filePath
	 * @throws IllegalArgumentException: filePath be null or empty
	 * @return
	 */
	public static boolean isExistedFilePath(String filePath){
		effectiveStr(filePath);
		return isExistedFile(new File(filePath));
  	}

	/**
	 * Checks for effective data element with exception
	 *
	 * @param data
	 */
	public static void effectiveData(byte[] data) {
		if (!isEffectiveData(data)) {
			throw new IllegalArgumentException("illegal data");
		}
	}

	/**
	 * Checks for effective data element
	 *
	 * @param data
	 */
	public static boolean isEffectiveData(byte[] data) {
		return data != null && data.length > 0;
	}

	/**
	 * Checks for effective string with exception
	 *
	 * @param str
	 */
	public static void effectiveStr(String str) {
		if (!isEffectiveStr(str)) {
			throw new IllegalArgumentException("illegal string");
		}
	}

	/**
	 * Check for the number if be positive(>=0)
	 * @param number
	 */
	public static void isPositive(Number number) {
		if (number.doubleValue() <= 0)
			throw new IllegalArgumentException(
					"number shouldn't < 0, current number is: " + number);
	}

	/**
	 * Checks for effective string with judgement
	 *
	 * @param str
	 * @return
	 */
	public static boolean isEffectiveStr(String str) {
		return str != null && str.length() > 0;
	}

	/**
	 * To reduce the system activity, it should limit some primary parameters
	 * for client.
	 *
	 * @author Denny Ye
	 *
	 */
	public static class Constants {

		/**
		 * Client data limitation (16MB)
		 */
		public static final int DATA_SIZE_LIMIT = 16777216;

		/**
		 * Default replica factor
		 */
		public static final int DEFAULT_REPLICA = 3;

		/**
		 * To reduce the storage space
		 */
		public static final int KEY_SIZE_LIMIT = 1024;

	}

}
