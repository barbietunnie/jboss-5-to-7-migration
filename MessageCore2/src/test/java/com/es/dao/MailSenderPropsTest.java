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

import com.es.dao.smtp.MailSenderPropsDao;
import com.es.vo.comm.MailSenderVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class MailSenderPropsTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private MailSenderPropsDao mailSenderPropsDao;

	@BeforeClass
	public static void MailSenderPropsPrepare() {
	}
	
	@Test
	public void testMailSenderProps() {
		try {
			List<MailSenderVo> list = selectAll();
			assertTrue(list.size()>0);
			MailSenderVo vo = selectByPrimaryKey(list.get(0).getRowId());
			assertNotNull(vo);
			MailSenderVo vo2 = insert(vo.getRowId());
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			vo.setInternalLoopback(vo2.getInternalLoopback());
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(vo2.getRowId());
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private List<MailSenderVo> selectAll() {
		List<MailSenderVo> mailSenderPropses = mailSenderPropsDao.getAll();
		for (Iterator<MailSenderVo> it=mailSenderPropses.iterator(); it.hasNext();) {
			MailSenderVo mailSenderVo = it.next();
			System.out.println("MailSenderPropsDao - selectAll: "+LF+mailSenderVo);
		}
		return mailSenderPropses;
	}
	
	public MailSenderVo selectByPrimaryKey(int rowId) {
		MailSenderVo vo = mailSenderPropsDao.getByPrimaryKey(rowId);
		System.out.println("MailSenderPropsDao - selectByPrimaryKey: "+LF+vo);
		return vo;
	}
	
	private int update(MailSenderVo mailSenderVo) {
		if ("Yes".equalsIgnoreCase(mailSenderVo.getUseTestAddr())) {
			mailSenderVo.setUseTestAddr("yes");
		}
		int rows = mailSenderPropsDao.update(mailSenderVo);
		System.out.println("MailSenderPropsDao - update: rows updated "+rows);
		return rows;
	}
	
	private int deleteByPrimaryKey(int rowId) {
		int rowsDeleted = mailSenderPropsDao.deleteByPrimaryKey(rowId);
		System.out.println("MailSenderPropsDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	private MailSenderVo insert(int rowId) {
		MailSenderVo vo = mailSenderPropsDao.getByPrimaryKey(rowId);
		if (vo != null) {
			vo.setInternalLoopback(vo.getInternalLoopback() + "_test");
			int rows = mailSenderPropsDao.insert(vo);
			System.out.println("MailSenderPropsDao - insert: rows inserted "+rows);
			return selectByPrimaryKey(vo.getRowId());
		}
		return null;
	}
}
