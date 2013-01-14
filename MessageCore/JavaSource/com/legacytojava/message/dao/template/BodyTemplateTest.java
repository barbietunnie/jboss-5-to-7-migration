package com.legacytojava.message.dao.template;

import static org.junit.Assert.*;

import java.sql.Timestamp;
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

import com.legacytojava.message.vo.template.BodyTemplateVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class BodyTemplateTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private BodyTemplateDao bodyTemplateDao;
	Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
	final String testTemplateId = "WeekendDeals";

	@BeforeClass
	public static void BodyTemplatePrepare() {
	}
	
	@Test
	public void testBodyTemplate() throws Exception {
		try {
			List<BodyTemplateVo> list = selectByTemplateId(testTemplateId);
			assertTrue(list.size()>0);
			BodyTemplateVo vo = selectByPrimaryKey(list.get(list.size()-1));
			assertNotNull(vo);
			BodyTemplateVo vo2 = insert(testTemplateId);
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setTemplateId(vo2.getTemplateId());
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(vo2);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private List<BodyTemplateVo> selectByTemplateId(String templateId) {
		List<BodyTemplateVo> variables = bodyTemplateDao.getByTemplateId(templateId);
		for (Iterator<BodyTemplateVo> it = variables.iterator(); it.hasNext();) {
			BodyTemplateVo bodyTemplateVo = it.next();
			System.out.println("BodyTemplateDao - selectByTemplateId: " + LF + bodyTemplateVo);
		}
		return variables;
	}

	private BodyTemplateVo selectByPrimaryKey(BodyTemplateVo vo) {
		BodyTemplateVo bodyvo = bodyTemplateDao.getByPrimaryKey(vo.getTemplateId(),
				vo.getClientId(), vo.getStartTime());
		if (bodyvo!=null) {
			System.out.println("BodyTemplateDao - selectByPrimaryKey: " + LF + bodyvo);
		}
		return bodyvo;
	}

	private int update(BodyTemplateVo bodyTemplateVo) {
		bodyTemplateVo.setTemplateValue("Dear customer, here is a list of great deals on gardening tools provided to you by ${company.url}");
		int rows = bodyTemplateDao.update(bodyTemplateVo);
		System.out.println("BodyTemplateDao - update: rows updated " + rows);
		return rows;
	}

	private BodyTemplateVo insert(String templateId) {
		List<BodyTemplateVo> list = bodyTemplateDao.getByTemplateId(templateId);
		if (list.size() > 0) {
			BodyTemplateVo vo = list.get(list.size() - 1);
			vo.setTemplateId(vo.getTemplateId()+"_v2");
			int rows = bodyTemplateDao.insert(vo);
			System.out.println("BodyTemplateDao - insert: rows inserted " + rows);
			return selectByPrimaryKey(vo);
		}
		return null;
	}

	private int deleteByPrimaryKey(BodyTemplateVo vo) {
		int rowsDeleted = bodyTemplateDao.deleteByPrimaryKey(vo.getTemplateId(), vo.getClientId(),
				vo.getStartTime());
		System.out.println("BodyTemplateDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}
