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

import com.legacytojava.message.vo.template.ClientVariableVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class ClientVariableTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private ClientVariableDao clientVariableDao;
	Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
	final String testVariableName = "CurrentDate";

	@BeforeClass
	public static void ClientVariablePrepare() {
	}
	
	@Test
	public void testClientVariables() throws Exception {
		try {
			List<ClientVariableVo> lst1 = selectByVariableName(testVariableName);
			assertTrue(lst1.size()>0);
			List<ClientVariableVo> lst2 = selectByClientId(lst1.get(0).getClientId());
			assertTrue(lst2.size()>0);
			ClientVariableVo vo = selectByPromaryKey(lst2.get(lst2.size()-1));
			assertNotNull(vo);
			ClientVariableVo vo2 = insert(lst2.get(lst2.size()-1).getClientId());
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
			throw e;
		}
	}

	private List<ClientVariableVo> selectByVariableName(String varbleName) {
		List<ClientVariableVo> variables = clientVariableDao.getByVariableName(varbleName);
		for (Iterator<ClientVariableVo> it = variables.iterator(); it.hasNext();) {
			ClientVariableVo vo = it.next();
			System.out.println("ClientVariableDao - selectByVariableName: " + LF + vo);
		}
		return variables;
	}

	private List<ClientVariableVo> selectByClientId(String clientId) {
		List<ClientVariableVo> list = clientVariableDao.getCurrentByClientId(clientId);
		System.out.println("ClientVariableDao - selectByClientId: rows returned: " + list.size());
		return list;
	}

	private ClientVariableVo selectByPromaryKey(ClientVariableVo vo) {
		ClientVariableVo client = clientVariableDao.getByPrimaryKey(vo.getClientId(), vo.getVariableName(), vo.getStartTime());
		if (client!=null) {
			System.out.println("ClientVariableDao - selectByPromaryKey: " + LF + client);
		}
		return client;
	}
	private int update(ClientVariableVo clientVariableVo) {
		clientVariableVo.setVariableValue(updtTime.toString());
		int rows = clientVariableDao.update(clientVariableVo);
		System.out.println("ClientVariableDao - update: rows updated " + rows);
		return rows;
	}

	private ClientVariableVo insert(String clientId) {
		List<ClientVariableVo> list = clientVariableDao.getCurrentByClientId(clientId);
		if (list.size() > 0) {
			ClientVariableVo vo = list.get(list.size() - 1);
			vo.setStartTime(new Timestamp(new java.util.Date().getTime()));
			int rows = clientVariableDao.insert(vo);
			System.out.println("ClientVariableDao - insert: rows inserted " + rows);
			return selectByPromaryKey(vo);
		}
		return null;
	}

	private int deleteByPrimaryKey(ClientVariableVo vo) {
		int rowsDeleted = clientVariableDao.deleteByPrimaryKey(vo.getClientId(),
				vo.getVariableName(), vo.getStartTime());
		System.out.println("ClientVariableDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}
