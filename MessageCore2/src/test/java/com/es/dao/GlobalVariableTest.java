package com.es.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

import com.es.dao.sender.GlobalVariableDao;
import com.es.data.constant.CodeType;
import com.es.data.constant.StatusId;
import com.es.data.constant.VariableType;
import com.es.data.preload.GlobalVariableEnum;
import com.es.vo.template.GlobalVariableVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class GlobalVariableTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private GlobalVariableDao globalVariableDao;
	Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
	final String testVariableName = GlobalVariableEnum.CurrentDate.name();

	@BeforeClass
	public static void GlobalVariablePrepare() {
	}
	
	@Test
	public void testGlobalVariable() {
		try {
			List<GlobalVariableVo> list = selectByVariableName(testVariableName);
			assertTrue(list.size()>0);
			GlobalVariableVo vo = selectByPrimaryKey(list.get(list.size()-1));
			assertNotNull(vo);
			GlobalVariableVo vo2 = insert(vo.getVariableName());
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setStartTime(vo2.getStartTime());
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

	private List<GlobalVariableVo> selectByVariableName(String varbleName) {
		List<GlobalVariableVo> variables = globalVariableDao.getByVariableName(varbleName);
		for (Iterator<GlobalVariableVo> it = variables.iterator(); it.hasNext();) {
			GlobalVariableVo vo = it.next();
			System.out.println("GlobalVariableDao - selectByVariableName: " + LF + vo);
		}
		return variables;
	}

	private GlobalVariableVo selectByPrimaryKey(GlobalVariableVo _vo) {
		GlobalVariableVo vo = globalVariableDao.getByPrimaryKey(_vo.getVariableName(), _vo.getStartTime());
		if (vo!=null) {
			System.out.println("GlobalVariableDao - selectByPrimaryKey: " + LF + vo);
		}
		return vo;
	}

	private int update(GlobalVariableVo globalVariableVo) {
		globalVariableVo.setVariableValue(updtTime.toString());
		int rows = globalVariableDao.update(globalVariableVo);
		System.out.println("GlobalVariableDao - update: rows upadted " + rows);
		return rows;
	}

	private GlobalVariableVo insert(String varbleName) {
		List<GlobalVariableVo> list = globalVariableDao.getByVariableName(varbleName);
		GlobalVariableVo vo = null;
		if (list.size() > 0) {
			vo = list.get(list.size() - 1);
			vo.setStartTime(new Timestamp(new java.util.Date().getTime()));
		}
		else {
			vo = new GlobalVariableVo();
			vo.setVariableName(varbleName);
			vo.setStartTime(new Timestamp(System.currentTimeMillis()));
			vo.setVariableType(VariableType.TEXT.getValue());
			vo.setStatusId(StatusId.ACTIVE.getValue());
			vo.setAllowOverride(CodeType.YES_CODE.getValue());
			vo.setRequired(CodeType.NO_CODE.getValue());
			vo.setVariableValue("Test Variable Value");
		}
		int rows = globalVariableDao.insert(vo);
		System.out.println("GlobalVariableDao - insert: rows inserted " + rows);
		return selectByPrimaryKey(vo);
	}

	private int deleteByPrimaryKey(GlobalVariableVo vo) {
		int rowsDeleted = globalVariableDao.deleteByPrimaryKey(vo.getVariableName(),
				vo.getStartTime());
		System.out.println("GlobalVariableDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}
