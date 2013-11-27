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

import com.es.dao.template.TemplateVariableDao;
import com.es.vo.template.TemplateVariableVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class TemplateVariableTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private TemplateVariableDao templateVariableDao;
	Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
	final String testVariableName = "CurrentDate";

	@BeforeClass
	public static void TemplateVariablePrepare() {
	}
	
	@Test
	public void testTemplateVariable() {
		try {
			List<TemplateVariableVo> list = selectByVariableName(testVariableName);
			assertTrue(list.size()>0);
			TemplateVariableVo vo = selectByPrimaryKey(list.get(list.size()-1));
			assertNotNull(vo);
			TemplateVariableVo vo2 = insert(vo.getVariableName());
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setTemplateId(vo2.getTemplateId());
			assertTrue(vo.equalsTo(vo2));
			int rows = update(vo2);
			assertEquals(rows, 1);
			rows = deleteByPrimaryKey(vo2);
			assertEquals(rows, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private List<TemplateVariableVo> selectByVariableName(String vname) {
		List<TemplateVariableVo> variables = templateVariableDao.getByVariableName(vname);
		for (Iterator<TemplateVariableVo> it = variables.iterator(); it.hasNext();) {
			TemplateVariableVo templateVariableVo = it.next();
			System.out.println("TemplateVariableDao - selectByVariableName: " + LF
					+ templateVariableVo);
		}
		return variables;
	}

	private TemplateVariableVo selectByPrimaryKey(TemplateVariableVo vo) {
		TemplateVariableVo tmplt = templateVariableDao.getByPrimaryKey(vo.getTemplateId(),
				vo.getSenderId(), vo.getVariableName(), vo.getStartTime());
		if (tmplt!=null) {
			System.out.println("TemplateVariableDao - selectByPrimaryKey: " + LF + tmplt);
		}
		return tmplt;
	}

	private int update(TemplateVariableVo vo) {
		vo.setVariableValue(updtTime.toString());
		int rows = templateVariableDao.update(vo);
		System.out.println("TemplateVariableDao - update: rows updated " + rows);
		return rows;
	}

	private TemplateVariableVo insert(String vname) {
		List<TemplateVariableVo> list = templateVariableDao.getByVariableName(vname);
		if (list.size() > 0) {
			TemplateVariableVo templateVariableVo = list.get(list.size() - 1);
			templateVariableVo.setTemplateId(templateVariableVo.getTemplateId()+"_v2");
			int rows = templateVariableDao.insert(templateVariableVo);
			System.out.println("TemplateVariableDao - insert: rows inserted " + rows);
			return selectByPrimaryKey(templateVariableVo);
		}
		throw new IllegalStateException("Should not be here, programming error!");
	}

	private int deleteByPrimaryKey(TemplateVariableVo vo) {
		int rows = templateVariableDao.deleteByPrimaryKey(vo.getTemplateId(), vo.getSenderId(),
				vo.getVariableName(), vo.getStartTime());
		System.out.println("TemplateVariableDao - deleteByPrimaryKey: Rows Deleted: " + rows);
		return rows;
	}
}
