package jpa.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.NoResultException;
import javax.sql.DataSource;

import jpa.model.UserData;
import jpa.service.UserDataService;
import jpa.util.SpringUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class UserDataTest {

	@BeforeClass
	public static void UserDataPrepare() {
	}

	@Autowired
	UserDataService service;

	@Test
	public void userDataService() {
		DataSource ds = (DataSource) SpringUtil.getAppContext().getBean("msgDataSource");
		Connection con = null;
		try {
			con = ds.getConnection();
			//System.err.println("AutoCommit?" + con.getAutoCommit());
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			if (con!=null) {
				try {
					con.close();
				} catch (SQLException e) {}
			}
		}
		
		List<UserData> list = service.getAll();
		assertFalse(list.isEmpty());
		
		UserData tkn0 = service.getByUserId(list.get(0).getUserId());
		assertNotNull(tkn0);
		
		tkn0 = service.getByRowId(list.get(0).getRowId());
		assertNotNull(tkn0);
		
		// test update
		tkn0.setUpdtUserId("JpaTest");
		service.update(tkn0);
		
		UserData tkn1 = service.getByRowId(tkn0.getRowId());
		assertTrue("JpaTest".equals(tkn1.getUpdtUserId()));
		// end of test update
		
		// test insert
		UserData tkn2 = new UserData();
		try {
			SqlTimestampConverter converter1 = new SqlTimestampConverter(null);
			ConvertUtils.register(converter1, java.sql.Timestamp.class);
			BeanUtils.copyProperties(tkn2, tkn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		tkn2.setUserId(tkn1.getUserId()+"_v2");
		service.insert(tkn2);
		
		UserData tkn3 = service.getByUserId(tkn2.getUserId());
		assertTrue(tkn3.getRowId()!=tkn1.getRowId());
		// end of test insert
		
		// test select with NoResultException
		service.delete(tkn3);
		try {
			service.getByUserId(tkn2.getUserId());
			fail();
		}
		catch (NoResultException e) {
		}
		
		assertTrue(0==service.deleteByUserId(tkn3.getUserId()));
		assertTrue(0==service.deleteByRowId(tkn3.getRowId()));
	}
}
