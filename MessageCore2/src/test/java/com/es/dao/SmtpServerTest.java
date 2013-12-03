package com.es.dao;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.smtp.SmtpServerDao;
import com.es.data.constant.StatusId;
import com.es.vo.comm.SmtpServerVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class SmtpServerTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private SmtpServerDao smtpServerDao;
	
	@BeforeClass
	public static void SmtpServerPrepare() {
	}
	
	@Test
	public void testSmtpServer() {
		try {
			List<SmtpServerVo> lst1 = selectAll(true);
			assertTrue(lst1.size()>0);
			List<SmtpServerVo> lst2 = selectAll(false);
			assertTrue(lst2.size()>0);
			SmtpServerVo vo = selectByPrimaryKey(lst2.get(0).getServerName());
			assertNotNull(vo);
			SmtpServerVo vo2 = insert(lst2.get(0).getServerName());
			int rowsUpdated = update(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			vo.setServerName(vo2.getServerName());
			assertTrue(vo.equalsTo(vo2));
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = delete(vo2.getServerName());
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private List<SmtpServerVo> selectAll(boolean forTrial) {
		List<SmtpServerVo> smtpServeres;
		if (forTrial) {
			smtpServeres = smtpServerDao.getAllForTrial(false);
			for (Iterator<SmtpServerVo> it=smtpServeres.iterator(); it.hasNext();) {
				SmtpServerVo smtpConnVo = it.next();
				System.out.println("SmtpServerDao - getAllForTrial(): "+LF+smtpConnVo);
			}
		}
		else {
			smtpServeres = smtpServerDao.getAll(false, null);
			for (Iterator<SmtpServerVo> it=smtpServeres.iterator(); it.hasNext();) {
				SmtpServerVo smtpConnVo = it.next();
				System.out.println("SmtpServerDao - selectAll(): "+LF+smtpConnVo);
			}
		}
		return smtpServeres;
	}
	
	public SmtpServerVo selectByPrimaryKey(String serverName) {
		SmtpServerVo vo2 = smtpServerDao.getByPrimaryKey(serverName);
		System.out.println("SmtpServerDao - selectByPrimaryKey: "+LF+vo2);
		return vo2;
	}
	
	private int update(SmtpServerVo smtpConnVo) {
		if (StatusId.ACTIVE.getValue().equals(smtpConnVo.getStatusId())) {
			smtpConnVo.setStatusId(StatusId.ACTIVE.getValue());
		}
		int rows = smtpServerDao.update(smtpConnVo);
		System.out.println("SmtpServerDao - update: rows updated " + rows );
		return rows;
	}
	
	private int delete(String serverName) {
		int rowsDeleted = smtpServerDao.deleteByPrimaryKey(serverName);
		System.out.println("SmtpServerDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	private SmtpServerVo insert(String serverName) {
		SmtpServerVo smtpConnVo = smtpServerDao.getByPrimaryKey(serverName);
		if (smtpConnVo != null) {
			smtpConnVo.setServerName(smtpConnVo.getServerName()+"_test");
			int rows = smtpServerDao.insert(smtpConnVo);
			System.out.println("SmtpServerDao - insert: rows inserted "+rows);
			return selectByPrimaryKey(smtpConnVo.getServerName());
		}
		throw new IllegalStateException("Should not be here, programming error!");
	}
}
