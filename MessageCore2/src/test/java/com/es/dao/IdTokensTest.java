package com.es.dao;

import static org.junit.Assert.assertEquals;
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

import com.es.dao.idtoken.IdTokensDao;
import com.es.dao.sender.SenderDao;
import com.es.vo.comm.IdTokensVo;
import com.es.vo.comm.SenderVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class IdTokensTest {
	@Resource
	private IdTokensDao idTokensDao;
	@Resource
	private SenderDao senderDao;
	private String testSenderId = "JBatchCorp";
	
	@BeforeClass
	public static void IdTokensPrepare() {
	}
	
	@Test
	public void insertUpdateDelete() {
		try {
			IdTokensVo vo = insert();
			assertNotNull(vo);
			IdTokensVo vo2 = selectBySenderId(vo.getSenderId());
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
			vo.setSenderId(testSenderId);
			delete(vo);
			e.printStackTrace();
			fail();
		}
	}
	
	private IdTokensVo selectBySenderId(String senderId) {
		IdTokensVo vo = idTokensDao.getBySenderId(senderId);
		if (vo != null) {
			System.out.println("IdTokensDao: selectBySenderId "+vo);
		}
		return vo;
	}

	private int update(IdTokensVo idTokensVo) {
		IdTokensVo vo = idTokensDao.getBySenderId(idTokensVo.getSenderId());
		vo.setDescription("For Test SenderId");
		int rows = idTokensDao.update(vo);
		System.out.println("IdTokensDao: update "+rows+"\n"+vo);
		return rows;
	}
	private IdTokensVo insert() {
		if (senderDao.getBySenderId(testSenderId)==null) {
			List<SenderVo> list = senderDao.getAll();
			testSenderId = list.get(0).getSenderId();
		}
		List<IdTokensVo> list = idTokensDao.getAll();
		for (IdTokensVo vo : list) {
			if (testSenderId.equals(vo.getSenderId())) {
				delete(vo);
			}
			vo.setSenderId(testSenderId);
			idTokensDao.insert(vo);
			System.out.println("IdTokensDao: insert "+vo);
			return vo;
		}
		return null;
	}
	private int delete(IdTokensVo idTokensVo) {
		int rowsDeleted = idTokensDao.delete(idTokensVo.getSenderId());
		System.out.println("IdTokensDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
}
