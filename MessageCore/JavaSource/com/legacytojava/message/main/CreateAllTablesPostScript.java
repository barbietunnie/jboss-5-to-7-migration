package com.legacytojava.message.main;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.bo.inbox.RuleEngineTest;
import com.legacytojava.message.bo.outbox.MsgOutboxBoTest;
import com.legacytojava.message.dao.inbox.MsgStreamDao;
import com.legacytojava.message.dao.outbox.MsgRenderedDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=false)
@Transactional
public class CreateAllTablesPostScript {
	@Resource
	private MsgStreamDao msgStreamDao;
	@Resource
	private MsgRenderedDao msgRenderedDao;
	@Test
	public void insertMsgStream() {
		if (msgStreamDao.getLastRecord()==null) {
			Result result = JUnitCore.runClasses(RuleEngineTest.class);
			for (Failure failure : result.getFailures()) {
				System.err.println(failure.toString());
			}
			System.out.println("Rule Engine test completed.");
		}
	}
	@Test
	public void insertMsgRendered() {
		if (msgRenderedDao.getLastRecord()==null) {
			Result result = JUnitCore.runClasses(MsgOutboxBoTest.class);
			for (Failure failure : result.getFailures()) {
				System.err.println(failure.toString());
			}
			System.out.println("MsgOutbox BO test completed.");
		}
	}

}
