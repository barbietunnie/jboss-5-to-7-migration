package com.legacytojava.message.dao.socket;

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

import com.legacytojava.message.vo.SocketServerVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class SocketServerTest {
	@Resource
	private SocketServerDao socketServerDao;
	
	@BeforeClass
	public static void SocketServerPrepare() {
	}
	
	@Test
	public void testSocketServer() {
		try {
			List<SocketServerVo> svrs = socketServerDao.getAll(true);
			assertFalse(svrs.isEmpty());
			SocketServerVo vo1 = socketServerDao.getByPrimaryKey(svrs.get(0).getServerName());
			assertNotNull(vo1);
			int rowsUpdated  = update(vo1);
			assertTrue(rowsUpdated>0);
			SocketServerVo vo2 = insert();
			int rowsDeleted = delete(vo2);
			assertTrue(rowsDeleted>0);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private int update(SocketServerVo vo) {
		if (!"A".equals(vo.getStatusId())) {
			vo.setStatusId("A");
		}
		int rows = socketServerDao.update(vo);
		System.out.println("SocketServerDao - update: "+vo);
		return rows;
	}
	
	private int delete(SocketServerVo socketServerVo) {
		int rowsDeleted = socketServerDao.deleteByPrimaryKey(socketServerVo.getServerName());
		System.out.println("SocketServerDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}

	private SocketServerVo insert() {
		List<SocketServerVo> socketServeres = socketServerDao.getAll(false);
		if (socketServeres.size()>0) {
			SocketServerVo socketServerVo = socketServeres.get(socketServeres.size()-1);
			socketServerVo.setServerName(socketServerVo.getServerName()+"_v2");
			socketServerVo.setSocketPort(socketServerVo.getSocketPort()+10);
			socketServerDao.insert(socketServerVo);
			System.out.println("SocketServerDao - insert: "+socketServerVo);
			return socketServerVo;
		}
		throw new IllegalStateException("SocketServers table is empty.");
	}
}
