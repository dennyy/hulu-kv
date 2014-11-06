/**
 * 
 */
package org.space.hulu.entry;

import junit.framework.Assert;
import net.sf.json.JSONObject;

import org.junit.Test;

/**
 * @author pelu2
 * @date Jun 19, 2012
 */
public class StorageInfoTest {
	
	@Test
	public void test(){
		StorageInfo info = new StorageInfo();
		info.setHost("test");
		info.setPort(80);
		
		JSONObject json = JSONObject.fromObject(info);
		
		String content = (!json.isNullObject() && !json.isEmpty()) ? json.toString() : "";

		System.out.print(content);
//		
		JSONObject tgtJson = JSONObject.fromObject(content);
//
		StorageInfo tgtInfo = (StorageInfo) JSONObject.toBean(tgtJson, StorageInfo.class);
		
	}

}
