package com.legacytojava.message.dao.idtokens;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.vo.IdTokensVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class IdTokensTest {
	@Resource
	private IdTokensDao idTokensDao;
	final String testClientId = "JBatchCorp";
	
	@BeforeClass
	public static void IdTokensPrepare() {
	}
	
	@Test
	public void insertUpdateDelete() {
		try {
			IdTokensVo vo = insert();
			assertNotNull(vo);
			IdTokensVo vo2 = selectByClientId(vo.getClientId());
			assertNotNull(vo2);
			vo2.setOrigUpdtTime(vo.getOrigUpdtTime());
			vo2.setUpdtTime(vo.getUpdtTime());
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = delete(vo);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			IdTokensVo vo = new IdTokensVo();
			vo.setClientId(testClientId);
			delete(vo);
			
			e.printStackTrace();
		}
	}
	
	private IdTokensVo selectByClientId(String clientId) {
		IdTokensVo vo = idTokensDao.getByClientId(clientId);
		if (vo != null) {
			System.out.println("IdTokensDao: selectByClientId "+vo);
		}
		return vo;
	}

	private int update(IdTokensVo idTokensVo) {
		IdTokensVo vo = idTokensDao.getByClientId(idTokensVo.getClientId());
		vo.setDescription("For Test SenderId");
		int rows = idTokensDao.update(vo);
		System.out.println("IdTokensDao: update "+rows+"\n"+vo);
		return rows;
	}
	private IdTokensVo insert() {
		List<IdTokensVo> list = idTokensDao.getAll();
		for (IdTokensVo vo : list) {
			vo.setClientId(testClientId);
			idTokensDao.insert(vo);
			System.out.println("IdTokensDao: insert "+vo);
			return vo;
		}
		return null;
	}
	private int delete(IdTokensVo idTokensVo) {
		int rowsDeleted = idTokensDao.delete(idTokensVo.getClientId());
		System.out.println("IdTokensDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
}
