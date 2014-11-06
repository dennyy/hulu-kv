package org.space.hulu.storage.file;

import java.util.List;

import org.space.hulu.entry.storage.FileMeta;

/**
 * Service to file consistency 
 * 
 * @Author Denny Ye
 * @Since 2012-7-4
 */
public interface FileCompareService {

	/**
	 * Report local files to peer of other Storage server in this group.
	 * 
	 * @param localFiles
	 * @return
	 */
	List<FileMeta> reportFileMetas(List<FileMeta> localFiles);
	
}


