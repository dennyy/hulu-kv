/**
 * 
 */
package org.space.hulu.zookeeper;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.space.hulu.entry.StorageGroup;
import org.space.hulu.entry.StorageInfo;
import org.space.hulu.util.TypeConverter;

/**
 * @author pelu2
 * @date Jun 11, 2012
 */
public class ZookeeperUtil4ServerTest {

	@Test
	public void test(){
		ZkStorageAccessor zk = new ZkStorageAccessor();
		StorageInfo info = new StorageInfo();
		info.setHost("test");
		info.setPort(80);
		
		StorageGroup group = new StorageGroup(666);
		group.setReplicaFactor((short)3);
		
		try {
			zk.register2ZK(group, info);
			
			zk.updateInfo2ZK(666, info);
			
			List<StorageGroup> groupLst = zk.getAllGroups();
			
			System.out.println(groupLst);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	
	public static void main(String...strings){
		
		short s = (short) 10;
		
		byte[] s1 = TypeConverter.shortToByteArray(s);
		
		short result = TypeConverter.byteArrayToShort(s1);
        
		if(result != s){
			System.out.println("Wrong.");
		}else{
			System.out.println("right.");
		}

		
//		Random rand = new Random(System.currentTimeMillis());
//		byte[] b;
//		int a, v;
//		for (int i=0; i<10000000; i++) {
//		    a = rand.nextInt();
//		    b = TypeConverter.intToByteArray(a);
//		    v = TypeConverter.byteArrayToInt(b);
////		    if (a != v) {
//		        System.out.println("ERR! " + a + " != " + Arrays.toString(b) + " != " + v);
////		    }
//		}
//		System.out.println("Done!");
	}
	
}
