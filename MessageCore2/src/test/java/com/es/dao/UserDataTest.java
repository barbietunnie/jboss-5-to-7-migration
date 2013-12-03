package com.es.dao;

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

import com.es.dao.user.UserDataDao;
import com.es.data.constant.StatusId;
import com.es.vo.comm.UserDataVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class UserDataTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private UserDataDao userDao;
	@BeforeClass
	public static void UserPrepare() {
	}

	@Test
	public void testUser() {
		try {
			List<UserDataVo> list = selectAll();
			assertTrue(list.size()>0);
			UserDataVo vo = selectByPrimaryKey(list.get(0).getUserId());
			assertNotNull(vo);
			UserDataVo vo2 = insert(vo.getUserId());
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setUserId(vo2.getUserId());
			assertTrue(vo.equalsTo(vo2));
			int rows = update(vo2);
			assertEquals(rows,1);
			rows = delete(vo2);
			assertEquals(rows,1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private List<UserDataVo> selectAll() {
		List<UserDataVo> users = userDao.getAll(true);
		for (Iterator<UserDataVo> it=users.iterator(); it.hasNext();) {
			UserDataVo userVo = it.next();
			System.out.println("UserDataDao - selectAll: "+LF+userVo);
		}
		return users;
	}
	
	private UserDataVo selectByPrimaryKey(String userId) {
		UserDataVo vo2 = userDao.getByPrimaryKey(userId);
		if (vo2 != null) {
			System.out.println("UserDataDao - selectByPrimaryKey: "+LF+vo2);
		}
		return vo2;
	}
	private int update(UserDataVo userVo) {
		if (StatusId.ACTIVE.getValue().equals(userVo.getStatusId())) {
			userVo.setStatusId(StatusId.ACTIVE.getValue());
		}
		int rows = userDao.update(userVo);
		System.out.println("UserDataDao - update: rows updated "+rows);
		return rows;
	}
	
	private int delete(UserDataVo userVo) {
		int rowsDeleted = userDao.deleteByPrimaryKey(userVo.getUserId());
		System.out.println("UserDataDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	private UserDataVo insert(String userId) {
		UserDataVo userVo = userDao.getByPrimaryKey(userId);
		if (userVo!=null) {
			userVo.setUserId(userVo.getUserId()+"_v2");
			int rows = userDao.insert(userVo);
			System.out.println("UserDataDao - insert: rows inserted "+rows);
			return selectByPrimaryKey(userVo.getUserId());
		}
		return null;
	}
}
