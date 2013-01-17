package com.legacytojava.message.dao.user;

import static org.junit.Assert.*;

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

import com.legacytojava.message.vo.SessionUploadVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class SessionUploadTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private SessionUploadDao sessDao;
	final String testSessionId = "test_session_id";
	@BeforeClass
	public static void SessionUploadPrepare() {
	}
	@Test
	public void testSessionUpload() throws Exception {
		try {
			List<SessionUploadVo> list = selectBySessionId(testSessionId);
			assertTrue(list.size()>0);
			SessionUploadVo vo = selectByPrimaryKey(list.get(list.size()-1));
			assertNotNull(vo);
			SessionUploadVo vo2 = insert(vo.getSessionId());
			assertNotNull(vo2);
			vo.setCreateTime(vo2.getCreateTime());
			vo.setSessionSeq(vo2.getSessionSeq());
			assertTrue(vo.equalsTo(vo2));
			int rows = update(vo2);
			assertEquals(rows,1);
			rows = delete(vo2);
			assertEquals(rows,1);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private List<SessionUploadVo> selectBySessionId(String sessionId) {
		List<SessionUploadVo> list = sessDao.getBySessionId(sessionId);
		for (Iterator<SessionUploadVo> it=list.iterator(); it.hasNext();) {
			SessionUploadVo sessVo = it.next();
			System.out.println("SessionUploadDao - selectBySessionId: "+LF+sessVo);
		}
		return list;
	}
	
	private SessionUploadVo selectByPrimaryKey(SessionUploadVo vo) {
		SessionUploadVo vo2 = sessDao.getByPrimaryKey(vo.getSessionId(), vo.getSessionSeq());
		if (vo2 != null) {
			System.out.println("SessionUploadDao - selectByPrimaryKey: "+LF+vo2);
		}
		return vo2;
	}
	private int update(SessionUploadVo sessVo) {
		sessVo.setUserId(sessVo.getUserId());
		int rows = sessDao.update(sessVo);
		System.out.println("SessionUploadDao - update: rows updated "+rows);
		return rows;
	}
	
	private int delete(SessionUploadVo sessVo) {
		int rows = sessDao.deleteByPrimaryKey(sessVo.getSessionId(), sessVo.getSessionSeq());
		System.out.println("SessionUploadDao - delete: Rows Deleted: " + rows);
		return rows;
	}
	private SessionUploadVo insert(String sessionId) {
		List<SessionUploadVo> list = sessDao.getBySessionId(sessionId);
		if (list.size()>0) {
			SessionUploadVo sessVo = list.get(list.size() - 1);
			sessVo.setSessionSeq(sessVo.getSessionSeq() + 1);
			int rows = sessDao.insert(sessVo);
			System.out.println("SessionUploadDao - insert: rows inserted "+rows);
			return selectByPrimaryKey(sessVo);
		}
		return null;
	}
}
