package jpa.message.util;

import static org.junit.Assert.*;

import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Test;

public class MsgIdCipherJUnit {
	static final Logger logger = Logger.getLogger(MsgIdCipherJUnit.class);
	@Test 
	public void testMsgIdCipher() {
		long startTime =System.currentTimeMillis();
		Random random = new Random(startTime);
		int count = 0;
		for (int i = 0; i < 20000; i++) {
			int msgId = Math.abs(random.nextInt());
			String encoded1 = MsgIdCipher.encode(msgId);
			String encoded2 = MsgIdCipher.encode(msgId);
			if (encoded1 != null && !encoded1.equals(encoded2)) count++;
			int decoded1 = MsgIdCipher.decode(encoded1);
			int decoded2 = MsgIdCipher.decode(encoded2);
			assertEquals(msgId, decoded1);
			assertEquals(msgId, decoded2);
		}
		logger.info("Test completed, time taken: "
				+ (System.currentTimeMillis() - startTime)
				+ " ms, number of unequal encoding: " + count);
	}
}
