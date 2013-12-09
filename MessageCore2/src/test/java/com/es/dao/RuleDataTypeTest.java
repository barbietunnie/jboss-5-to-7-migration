package com.es.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.action.RuleDataTypeDao;
import com.es.data.preload.RuleDataTypeEnum;
import com.es.data.preload.RuleNameEnum;
import com.es.vo.action.RuleDataTypeVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class RuleDataTypeTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private RuleDataTypeDao ruleDataTypeDao;
	@BeforeClass
	public static void RuleDataTypePrepare() {
	}
	@Test
	public void testRuleDataType() throws Exception {
		try {
			RuleDataTypeVo vo0 = selectByTypeValuePair(RuleDataTypeEnum.RULE_NAME.name(), RuleNameEnum.HARD_BOUNCE.getValue());
			assertNotNull(vo0);
			List<RuleDataTypeVo> lst0 = ruleDataTypeDao.getByDataType(vo0.getDataType());
			assertFalse(lst0.isEmpty());
			RuleDataTypeVo vo1 = selectByPrimaryKey(vo0.getRowId());
			assertNotNull(vo1);
			RuleDataTypeVo vo2 = insert(vo1.getDataType());
			assertNotNull(vo2);
			vo1.setRowId(vo2.getRowId());
			vo1.setDataTypeValue(vo2.getDataTypeValue());
			assertTrue(vo1.equalsTo(vo2));
			int rows = update(vo2);
			assertEquals(rows, 1);
			rows = deleteByRowId(vo2);
			assertEquals(rows, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private RuleDataTypeVo selectByPrimaryKey(int rowId) {
		RuleDataTypeVo vo = ruleDataTypeDao.getByPrimaryKey(rowId);
		if (vo!=null) {
			System.out.println("RuleDataTypeDao - selectByPrimaryKey: "+LF+vo);
		}
		return vo;
	}
	
	private RuleDataTypeVo selectByTypeValuePair(String type, String value) {
		RuleDataTypeVo vo = ruleDataTypeDao.getByTypeValuePair(type, value);
		if (vo!=null) {
			System.out.println("RuleDataTypeDao - selectByTypeValuePair: "+vo);
		}
		return vo;
	}
	
	private int update(RuleDataTypeVo ruleDataTypeVo) {
		ruleDataTypeVo.setMiscProperties("Close the Email");
		int rows = ruleDataTypeDao.update(ruleDataTypeVo);
		System.out.println("RuleDataTypeDao - update: "+rows);
		return rows;
	}
	
	private RuleDataTypeVo insert(String dataType) {
		List<RuleDataTypeVo> list = ruleDataTypeDao.getByDataType(dataType);
		if (!list.isEmpty()) {
			RuleDataTypeVo ruleDataTypeVo = list.get(list.size()-1);
			ruleDataTypeVo.setDataTypeValue(ruleDataTypeVo.getDataTypeValue()+"_v2");
			int rows = ruleDataTypeDao.insert(ruleDataTypeVo);
			System.out.println("RuleDataTypeDao - insert: rows inserted "+rows);
			return selectByPrimaryKey(ruleDataTypeVo.getRowId());
		}
		throw new IllegalStateException("Should not be here, programming error!");
	}
	
	private int deleteByRowId(RuleDataTypeVo vo) {
		int rows = ruleDataTypeDao.deleteByPrimaryKey(vo.getRowId());
		System.out.println("RuleDataTypeDao - deleteByPrimaryKey: Rows Deleted: "+rows);
		return rows;
	}
}
