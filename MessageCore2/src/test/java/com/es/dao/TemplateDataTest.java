package com.es.dao;

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

import com.es.dao.template.TemplateDataDao;
import com.es.vo.template.TemplateDataVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class TemplateDataTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private TemplateDataDao templateDataDao;
	Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
	final String testTemplateId = "WeekendDeals";

	@BeforeClass
	public static void BodyTemplatePrepare() {
	}
	
	@Test
	public void testTemplateData() {
		try {
			List<TemplateDataVo> list = selectByTemplateId(testTemplateId);
			assertTrue(list.size()>0);
			TemplateDataVo vo = selectByPrimaryKey(list.get(list.size()-1));
			assertNotNull(vo);
			TemplateDataVo vo2 = insert(testTemplateId);
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
			fail();
		}
	}

	private List<TemplateDataVo> selectByTemplateId(String templateId) {
		List<TemplateDataVo> variables = templateDataDao.getByTemplateId(templateId);
		for (Iterator<TemplateDataVo> it = variables.iterator(); it.hasNext();) {
			TemplateDataVo templateVo = it.next();
			System.out.println("TemplateDataDao - selectByTemplateId: " + LF + templateVo);
		}
		return variables;
	}

	private TemplateDataVo selectByPrimaryKey(TemplateDataVo vo) {
		TemplateDataVo tmpltvo = templateDataDao.getByPrimaryKey(vo.getTemplateId(),
				vo.getSenderId(), vo.getStartTime());
		if (tmpltvo!=null) {
			System.out.println("TemplateDataDao - selectByPrimaryKey: " + LF + tmpltvo);
		}
		return tmpltvo;
	}

	private int update(TemplateDataVo templateVo) {
		templateVo.setBodyTemplate("Dear customer, here is a list of great deals on gardening tools provided to you by ${company.url}");
		templateVo.setSubjTemplate("Great deals from ${company.url}");
		int rows = templateDataDao.update(templateVo);
		System.out.println("TemplateDataDao - update: rows updated " + rows);
		return rows;
	}

	private TemplateDataVo insert(String templateId) {
		List<TemplateDataVo> list = templateDataDao.getByTemplateId(templateId);
		if (list.size() > 0) {
			TemplateDataVo vo = list.get(list.size() - 1);
			vo.setTemplateId(vo.getTemplateId()+"_v2");
			int rows = templateDataDao.insert(vo);
			System.out.println("TemplateDataDao - insert: rows inserted " + rows);
			return selectByPrimaryKey(vo);
		}
		throw new IllegalStateException("Should not be here, programming error!");
	}

	private int deleteByPrimaryKey(TemplateDataVo vo) {
		int rowsDeleted = templateDataDao.deleteByPrimaryKey(vo.getTemplateId(), vo.getSenderId(),
				vo.getStartTime());
		System.out.println("TemplateDataDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}
