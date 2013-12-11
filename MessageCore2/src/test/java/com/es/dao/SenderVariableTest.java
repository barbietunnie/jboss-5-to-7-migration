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

import com.es.dao.sender.SenderVariableDao;
import com.es.data.preload.GlobalVariableEnum;
import com.es.vo.template.SenderVariableVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class SenderVariableTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private SenderVariableDao senderVariableDao;
	Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
	final String testVariableName = GlobalVariableEnum.CurrentDate.name();

	@BeforeClass
	public static void SenderVariablePrepare() {
	}
	
	@Test
	public void testSenderVariables() {
		try {
			List<SenderVariableVo> lst1 = selectByVariableName(testVariableName);
			assertTrue(lst1.size()>0);
			List<SenderVariableVo> lst2 = selectBySenderId(lst1.get(0).getSenderId());
			assertTrue(lst2.size()>0);
			SenderVariableVo vo = selectByPromaryKey(lst2.get(lst2.size()-1));
			assertNotNull(vo);
			SenderVariableVo vo2 = insert(lst2.get(lst2.size()-1).getSenderId());
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

	private List<SenderVariableVo> selectByVariableName(String varbleName) {
		List<SenderVariableVo> variables = senderVariableDao.getByVariableName(varbleName);
		for (Iterator<SenderVariableVo> it = variables.iterator(); it.hasNext();) {
			SenderVariableVo vo = it.next();
			System.out.println("SenderVariableDao - selectByVariableName: " + LF + vo);
		}
		return variables;
	}

	private List<SenderVariableVo> selectBySenderId(String senderId) {
		List<SenderVariableVo> list = senderVariableDao.getCurrentBySenderId(senderId);
		System.out.println("SenderVariableDao - selectBySenderId: rows returned: " + list.size());
		return list;
	}

	private SenderVariableVo selectByPromaryKey(SenderVariableVo vo) {
		SenderVariableVo var = senderVariableDao.getByPrimaryKey(vo.getSenderId(), vo.getVariableName(), vo.getStartTime());
		if (var!=null) {
			System.out.println("SenderVariableDao - selectByPromaryKey: " + LF + var);
		}
		return var;
	}
	private int update(SenderVariableVo senderVariableVo) {
		senderVariableVo.setVariableValue(updtTime.toString());
		int rows = senderVariableDao.update(senderVariableVo);
		System.out.println("SenderVariableDao - update: rows updated " + rows);
		return rows;
	}

	private SenderVariableVo insert(String senderId) {
		List<SenderVariableVo> list = senderVariableDao.getCurrentBySenderId(senderId);
		if (list.size() > 0) {
			SenderVariableVo vo = list.get(list.size() - 1);
			vo.setStartTime(new Timestamp(new java.util.Date().getTime()));
			int rows = senderVariableDao.insert(vo);
			System.out.println("SenderVariableDao - insert: rows inserted " + rows);
			return selectByPromaryKey(vo);
		}
		throw new IllegalStateException("Should not be here, programming error!");
	}

	private int deleteByPrimaryKey(SenderVariableVo vo) {
		int rowsDeleted = senderVariableDao.deleteByPrimaryKey(vo.getSenderId(),
				vo.getVariableName(), vo.getStartTime());
		System.out.println("SenderVariableDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}
