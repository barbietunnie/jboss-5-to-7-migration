package com.legacytojava.message.dao.idtokens;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class MsgIdCipherJUnit {
	
	@Test 
	public void testMsgIdCipher() {
		long startTime = new java.util.Date().getTime();
		Random random = new Random(startTime);
		int count = 0;
		for (int i = 0; i < 20000; i++) {
			long msgId = Math.abs(random.nextLong());
			String encoded1 = MsgIdCipher.encode(msgId);
			String encoded2 = MsgIdCipher.encode(msgId);
			if (encoded1 != null && !encoded1.equals(encoded2)) count++;
			long decoded1 = MsgIdCipher.decode(encoded1);
			long decoded2 = MsgIdCipher.decode(encoded2);
			assertEquals(msgId, decoded1);
			assertEquals(msgId, decoded2);
		}
		System.out.println("Test completed, time taken: "
				+ (new java.util.Date().getTime() - startTime)
				+ " ms, number of unequal encoding: " + count);
	}
}
