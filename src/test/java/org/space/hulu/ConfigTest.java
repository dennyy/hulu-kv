package org.space.hulu;

import junit.framework.Assert;

import org.junit.Test;
import org.space.hulu.config.CommonConfig;

/**
 * Class Description
 *
 * @author Denny Ye
 * @since 2012-5-25
 * @version 1.0
 */
public class ConfigTest {

	@Test
	public void missingFile() {
		CommonConfig config = CommonConfig.getInstance();
		Assert.assertEquals(11, config.getInt("aa", 11));
		Assert.assertEquals("hdfs://10.224.194.111:9000", config.get("fs.default.name", ""));
	}
	
}

