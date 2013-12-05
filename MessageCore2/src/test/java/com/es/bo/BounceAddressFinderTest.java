package com.es.bo;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.bo.inbox.BounceAddressFinder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class BounceAddressFinderTest {
	static final Logger logger = Logger.getLogger(BounceAddressFinderTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	static final String LF = System.getProperty("line.separator", "\n");

	@Test
	public void findBounceAddr() {
		InputStream is = getClass().getResourceAsStream("bouncedmails/bounceBodySamples2.txt");
		if (is == null) {
			logger.warn("InputStream not found.");
			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			StringBuilder sb = new StringBuilder();
			String line = null;
			int count = 0;
			int matched = 0;
			while ((line = br.readLine()) != null) {
				if ("--- EOM ---".equals(line)) {
					count++;
					String body = sb.toString();
					sb = new StringBuilder();
					String addr = BounceAddressFinder.getInstance().find(body);
					if (StringUtils.isEmpty(addr)) {
						logger.info("(" + count + ") - not matched ##########.");
					}
					else {
						logger.info("(" + count + ") - matched: " + addr);
						matched++;
					}
				}
				else {
					sb.append(line + LF);
				}
			}
			br.close();
			logger.info("Count: " + count + ", Matched: " + matched);
			assertEquals(count, matched);
		}
		catch (IOException e) {
			logger.error("Exception", e);
		}
	}

}
