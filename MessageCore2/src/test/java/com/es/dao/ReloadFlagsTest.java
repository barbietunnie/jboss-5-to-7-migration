package com.es.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.sender.ReloadFlagsDao;
import com.es.vo.comm.ReloadFlagsVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class ReloadFlagsTest {
	@Resource
	private ReloadFlagsDao reloadDao;
	
	@BeforeClass
	public static void ReloadFlagsPrepare() {
	}
	
	@Test
	@Rollback(true)
	public void testReloadFlags() {
		ReloadFlagsVo vo = select();
		assertNotNull(vo);
		int rowsUpdated = update(vo);
		assertEquals(rowsUpdated, 1);
		rowsUpdated = recordsChanged();
		assertTrue(rowsUpdated>0);
	}
	
	private ReloadFlagsVo select() {
		ReloadFlagsVo flags = reloadDao.select();
		System.out.println("ReloadFlagsDao - select: "+flags);
		return flags;
	}
	
	private int update(ReloadFlagsVo vo) {
		int rows = 0;
		if (vo!=null) {
			vo.setSenders(vo.getSenders() + 1);
			vo.setRules(vo.getRules() + 1);
			vo.setActions(vo.getActions() + 1);
			vo.setTemplates(vo.getTemplates() + 1);
			vo.setSchedules(vo.getSchedules() + 1);
			rows = reloadDao.update(vo);
			System.out.println("ReloadFlagsDao - update: rows updated "+rows);
		}
		return rows;
	}
	
	private int recordsChanged() {
		int rows = reloadDao.updateSenderReloadFlag();
		rows += reloadDao.updateRuleReloadFlag();
		rows += reloadDao.updateActionReloadFlag();
		rows += reloadDao.updateTemplateReloadFlag();
		rows += reloadDao.updateScheduleReloadFlag();
		return rows;
	}
}
