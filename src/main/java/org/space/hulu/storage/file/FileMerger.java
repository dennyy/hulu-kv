package org.space.hulu.storage.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.space.hulu.entry.storage.FilePointer;
import org.space.hulu.io.IOUtils;
import org.space.hulu.io.Packet;
import org.space.hulu.util.Validation;

/**
 * Merging two structured local files to common structured file,
 * it may be contains all of the packets if it is alive
 *
 * @author Denny Ye
 * @since 2012-7-4
 * @version 1.0
 */
public class FileMerger {
	private static final Log LOG = LogFactory.getLog(FileMerger.class);
	
	/**
	 * Merge two files into one.
	 * The final file may be much more than file limitation
	 * 
	 * @param firstFile
	 * @param secondFile
	 * @param targetPath
	 * @return final file path
	 * @throws IOException during process
	 */
	public static void merge(String firstFile, String secondFile, 
						String targetPath) throws IOException {
		Validation.effectiveStr(targetPath);
		
		File target = new File(targetPath);
		if (target.exists() || target.length() > 0) {
			throw new IllegalArgumentException("Invalid target : " + targetPath);
		}
		
		long start = System.currentTimeMillis();
		
		FileOutputStream targetOutput = null;
		RandomAccessFile firstInput = null;
		RandomAccessFile secondInput = null;
		
		int duplicated = 0;
		
		Set<String> keySet = new HashSet<String>(10000, 100);
		
		try {
			targetOutput = new FileOutputStream(target);
			firstInput = new RandomAccessFile(firstFile, "rw");
			secondInput = new RandomAccessFile(secondFile, "rw");
			
			duplicated = dumpAndFilter(firstInput, targetOutput, keySet, duplicated);
			duplicated = dumpAndFilter(secondInput, targetOutput, keySet, duplicated);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			IOUtils.closeOutputStream(targetOutput, LOG);
			IOUtils.closeRandomAccessFile(firstInput, LOG);
			IOUtils.closeRandomAccessFile(secondInput, LOG);
		}
		
		LOG.info("Merged finished. From '" + firstFile + "' and '" 
				+ secondFile + "' to '" + targetPath 
				+ "'. [packet:" + keySet.size() + ", duplicated:" + duplicated + "] in " 
				+ (System.currentTimeMillis() - start) + " ms");
	}
	
	private static int dumpAndFilter(RandomAccessFile reader, FileOutputStream output, 
				Set<String> keySet, int duplicated) throws IOException {
		
		int offset = (int) reader.getFilePointer();
		while (reader.length() != offset) {
			int nextLen = reader.readInt();
			byte[] packetBuf = new byte[nextLen];
			reader.read(packetBuf);
			
			Packet packet = Packet.resolvePacket(nextLen, packetBuf);
			if (!keySet.contains(packet.getKey())) {
				keySet.add(packet.getKey());
				
				output.write(IOUtils.int2byte(nextLen));
				output.write(packetBuf);
			} else {
				duplicated++;
			}
			
			offset = (int) reader.getFilePointer();
		}
		
		return duplicated;
	}
}

